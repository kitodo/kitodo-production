/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.filemanagement.locking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.api.filemanagement.LockingResult;

/**
 * This is the main class of lock management. In regular operation, there must
 * always be just one lock management for comprehensible reasons, for which the
 * class implements the singleton pattern. The lock management has been divided
 * into three classes for the sake of clarity, the actual lock management is in
 * this class and manages the locks as such. The stream management manages the
 * input and output streams flowing through the locks, and the immutable read
 * file management manages the temporary files that must be generated for the
 * immutable read lock and later deleted.
 */
public class LockManagement {
    /**
     * The singleton instance of the lock management. This class should be used
     * as singleton, meaning that there is only one lock management all over the
     * class loader. Everything else would not make sense or would even be
     * dangerous.
     */
    private static volatile LockManagement instance;

    private static final Logger logger = LogManager.getLogger(LockManagement.class);

    /**
     * This message is used in exceptions when a wrong object has been
     * submitted.
     */
    private static final String UNSUITABLE_LOCKING_RESULT = "The locking result is unsuitable for generating the stream: ";

    /**
     * Returns the singleton instance of the lock management. This class should
     * be used as singleton, meaning that there is only one lock management all
     * over the class loader. For the usage, only the singleton should be used
     * and no more instances of the class should exist. Everything else would
     * not make sense or would even be dangerous.
     *
     * @return the singleton instance of this class
     *
     * @see "https://en.wikipedia.org/wiki/Double-checked_locking"
     */
    public static LockManagement getInstance() {
        if (instance == null) {
            synchronized (LockManagement.class) {
                if (instance == null) {
                    instance = new LockManagement();
                }
            }
        }
        return instance;
    }

    private static GrantedAccess tryCast(LockingResult lockingResult) throws AccessDeniedException {
        if (!(lockingResult instanceof GrantedAccess)) {
            throw new AccessDeniedException(UNSUITABLE_LOCKING_RESULT + Objects.toString(lockingResult));
        }
        return (GrantedAccess) lockingResult;
    }

    /**
     * This map will list for each URI what permissions have been granted to
     * whom.
     */
    private Map<URI, GrantedPermissions> grantedPermissions = new HashMap<>();

    /**
     * Variable for accessing the immutable read file management.
     *
     * <p>
     * Note: The variable is package private for the unit test to check the
     * state of the immutable read file management.
     */
    private ImmutableReadFileManagement immutableReadFileManagement = new ImmutableReadFileManagement();

    /**
     * Variable for accessing the stream management.
     */
    private StreamManagement streamManagement = new StreamManagement();

    /**
     * Creates a new lock management. This constructor is private because it
     * must only be called once per operation.
     */
    private LockManagement() {
    }

    /**
     * Checks for a group of locks if they can be returned.
     *
     * @param uris
     *            the URIs on the locks to be returned
     * @throws IllegalStateException
     *             if there is still a stream flowing over the lock
     */
    private void canClose(Collection<URI> uris) {
        String exceptionMessagePrefix = "The lock cannot be released because there are still streams flowing for the following URIs: ";
        String exceptionMessage = uris.parallelStream().filter(streamManagement::isKnowingAnOpenStreamTo)
                .map(uri -> uri.toString()).collect(Collectors.joining(", ", exceptionMessagePrefix, ""));
        if (exceptionMessage.length() > exceptionMessagePrefix.length()) {
            throw new IllegalStateException(exceptionMessage);
        }
    }

    /**
     * Checks the ability to lock in a parallel fashion. The called test method
     * returns {@code null} if successful, and this method removes the
     * {@code null}s from the stream, leaving a stream of conflicts at the end.
     * If all locks could be granted, the stream will no longer contain any
     * elements here, indicating that all requested locks can be granted.
     *
     * @param user
     *            the user requesting the locks
     * @param lockRequests
     *            a stream of desired locks
     * @return a stream of locks that can not be granted as pairs of URI and the
     *         names of those users who hold locks that prevent the requested
     *         lock from being granted
     */
    private Stream<FutureMapEntry<URI, Collection<String>>> checkLockability(String user,
            Stream<Entry<URI, LockingMode>> lockRequests) {

        return lockRequests.map(
            entry -> new FutureMapEntry<>(entry.getKey(), checkLockability(user, entry.getKey(), entry.getValue())))
                .filter(entry -> !entry.getValue().isEmpty());
    }

    /**
     * Checks if a user can get a lock on an URI.
     *
     * @param user
     *            user who wants the lock
     * @param uri
     *            URI for which the lock is desired
     * @param requestedLock
     *            which lock is desired
     * @return all users who have locks that conflict with the requested lock
     */
    private Collection<String> checkLockability(String user, URI uri, LockingMode requestedLock) {
        if (grantedPermissions.get(uri) == null) {
            return Collections.emptyList();
        }
        return grantedPermissions.get(uri).checkLockability(user, requestedLock);
    }

    /**
     * Checks if a user is allowed to open a read channel on the file. If the
     * user is authorized to read the file, the method silently returns and
     * nothing happens. If not, an {@code AccessDeniedException} is thrown with
     * a meaningful justification. However, if the user code previously
     * submitted a request for appropriate permissions and verified that the
     * answer was positive, then this case should never happen.
     *
     * <p>
     * In case the user gets read access due to an immutable read lock, this
     * method returns the URI to the temporary copy of the main file. The
     * subsequent file access process must therefore open the read channel to
     * this temporary copy, not to the main file.
     *
     * @param lockingResult
     *            which user requests the write channel
     * @param uri
     *            for which file the channel is requested
     * @param write
     *            if true, also check for permission to write, else read only
     * @return the URI to use. This is the same one that was passed, except for
     *         the immutable read lock, where it is the URI of the immutable
     *         read copy.
     * @throws AccessDeniedException
     *             if the user does not have sufficient authorization
     * @throws ProtocolException
     *             if the file had to be first read in again, but this step was
     *             skipped on the protocol. This error can occur with the
     *             UPGRADE_WRITE_ONCE lock because its protocol form requires
     *             that the file must first be read in again and the input
     *             stream must be closed after the lock has been upgraded.
     */
    public URI checkPermission(LockingResult lockingResult, URI uri, boolean write)
            throws AccessDeniedException, ProtocolException {
        GrantedAccess permissions = tryCast(lockingResult);
        logger.trace("{} wants to open a {} channel to {}.", permissions.getUser(), write ? "writing" : "reading", uri);
        try {
            GrantedPermissions granted = grantedPermissions.get(uri);
            if (Objects.isNull(granted)) {
                throw new AccessDeniedException(permissions.getUser() + " claims to have a privilege to " + uri
                        + " he does not have at all. Whatever he did, it was wrong.");
            }
            granted.checkAuthorization(permissions.getUser(), permissions.getLock(uri), write);
        } catch (AccessDeniedException e) {
            logger.trace("{} is not allowed to open a {} channel to {}. The reason is: {}", permissions.getUser(),
                write ? "writing" : "reading", uri, e.getMessage());
            throw e;
        }
        AbstractLock lock = permissions.getLock(uri);
        if (lock instanceof ImmutableReadLock) {
            uri = ((ImmutableReadLock) lock).getImmutableReadCopyURI();
        }
        logger.trace("{} is allowed to open a {} channel to {}.", permissions.getUser(), write ? "writing" : "reading",
            uri);
        return uri;
    }

    /**
     * <b>This method must never be called!</b> It may only be used by tests to
     * reset the status of this instance.
     */
    void clear() {
        grantedPermissions = new HashMap<>();
        immutableReadFileManagement = new ImmutableReadFileManagement();
        streamManagement = new StreamManagement();
    }

    /**
     * Closes a set of permissions. First, all locks are checked for their
     * release. If that fails, an IOException is thrown. Otherwise, all locks
     * are removed from the permission set and possible temporary files are
     * logged off. This process can be parallelized.
     *
     * @param user
     *            user who returns the locks
     *
     * @param locks
     *            the locks that are returned
     * @throws IllegalStateException
     *             if there is still a stream flowing over the lock
     */
    void close(String user, Map<URI, AbstractLock> locks) {
        synchronized (this) {
            canClose(locks.keySet());
            locks.entrySet().parallelStream().map(Entry::getKey)
                    .peek(uri -> removePermission(user, locks.get(uri), uri))
                    .forEach(uri -> immutableReadFileManagement.maybeRemoveReadFile(uri, user));
        }
    }

    /**
     * Closes a permission from a set.
     *
     * @param access
     *            record from which the authorization is to be removed
     * @param uri
     *            URI for which the authorization is to be closed
     * @throws IllegalStateException
     *             if there is still a stream flowing over the lock
     */
    void close(GrantedAccess access, URI uri) {
        synchronized (this) {
            canClose(Arrays.asList(uri));
            removePermission(access.getUser(), access.getLock(uri), uri);
            immutableReadFileManagement.maybeRemoveReadFile(uri, access.getUser());
            access.forgetAccessTo(uri);
        }
    }

    /**
     * Generates a stream from the requested locks.
     *
     * @param lockRequests
     *            the requested locks
     * @return a stream from the requested locks
     */
    private Stream<Entry<URI, LockingMode>> createStreamOfLockRequests(LockRequests lockRequests) {
        return lockRequests.getLocks().entrySet().parallelStream();
    }

    /**
     * Returns an initialized granted permissions object from the granted
     * permissions map. If there is no such object, it will be created and
     * linked in the map.
     *
     * @param uri
     *            URI for which the object is to be created
     * @return an initialized granted permissions object
     */
    private GrantedPermissions getInitializedGrantedPermissions(URI uri) {
        GrantedPermissions granted = grantedPermissions.get(uri);
        if (granted == null) {
            granted = new GrantedPermissions(uri);
            grantedPermissions.put(uri, granted);
        }
        return granted;
    }

    /**
     * Sets the locks. Setting up the locks can be parallelized because it is
     * independent per URI. This makes sense here (depending on the content of
     * the meta-data files, for example, for year process files of newspapers),
     * there may be cases where several hundred URIs need to be locked at the
     * same time.
     *
     * @param requests
     *            the locks to set up
     * @param rights
     *            existing user rights to be extended
     * @throws UncheckedIOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    private GrantedAccess grantLocks(LockRequests requests, GrantedAccess rights) {
        Map<URI, AbstractLock> locks = createStreamOfLockRequests(requests)
                .map(requestedLock -> Pair.of(requestedLock.getKey(),
                    getInitializedGrantedPermissions(requestedLock.getKey()).addAGrantedLock(requests.getUser(),
                        requestedLock.getValue(), streamManagement, immutableReadFileManagement, rights)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        return new GrantedAccess(requests.getUser(), locks, this);
    }

    /**
     * Removes a permission from memory. If the administration object becomes
     * empty, the administration object is also deleted.
     *
     * @param user
     *            User who held the lock
     * @param lock
     *            lock (object)
     * @param uri
     *            locked URI
     */
    private void removePermission(String user, AbstractLock lock, URI uri) {
        GrantedPermissions permissions = grantedPermissions.get(uri);
        if (Objects.nonNull(permissions)) {
            permissions.remove(user, lock);
            if (permissions.isEmpty()) {
                grantedPermissions.remove(uri);
            }
        }
    }

    /**
     * With this method, file management reports that it has opened a read
     * channel for a URI. It wraps around the stream with a stream guard because
     * the lock management must be able to detect when the stream is shut down
     * because it depends on whether the lock in question can be reset, is
     * automatically reset, or other locks can be granted or not.
     * 
     * @param uri
     *            URI to which a read channel was opened
     * @param readChannel
     *            the opened input stream
     * @param lockingResult
     *            the authorization object
     * @return the input stream that the user should use. The input stream is
     *         wrapped in an instance of a vigilant input stream that notifies
     *         the lock management when the stream is closed.
     */
    public InputStream reportGrant(URI uri, InputStream readChannel, LockingResult lockingResult) {
        GrantedAccess permissions = (GrantedAccess) lockingResult;
        if (logger.isTraceEnabled()) {
            logger.trace("For {}, the reading channel {} was opened to {}.", permissions,
                Integer.toHexString(System.identityHashCode(readChannel)), uri);
        }
        AbstractLock lock = permissions.getLock(uri);
        if (lock instanceof UpgradeableReadLock) {
            ((UpgradeableReadLock) lock).noteReadingStarts();
        }
        VigilantInputStream vigilantInputStream = new VigilantInputStream(uri, readChannel, streamManagement,
                permissions);
        streamManagement.registerStreamGuard(vigilantInputStream);
        return vigilantInputStream;
    }

    /**
     * With this method, file management reports that it has opened a write
     * channel for a URI. It wraps around the stream with a stream guard because
     * the lock management must be able to detect when the stream is shut down
     * because it depends on whether the lock in question can be reset, is
     * automatically reset, or other locks can be granted or not.
     * 
     * @param uri
     *            URI to which a read channel was opened
     * @param outputStream
     *            the opened output stream
     * @param lockingResult
     *            the authorization object
     * @return the output stream that the user should use. The output stream is
     *         wrapped in an instance of a vigilant output stream that notifies
     *         the lock management when the stream is closed.
     */
    public VigilantOutputStream reportGrant(URI uri, OutputStream outputStream, LockingResult lockingResult) {
        GrantedAccess permissions = (GrantedAccess) lockingResult;
        if (logger.isTraceEnabled()) {
            String hexString = Integer.toHexString(System.identityHashCode(outputStream));
            logger.trace("For {}, the writing channel {} was opened to {}.", permissions, hexString, uri);
        }
        AbstractLock lock = permissions.getLock(uri);
        UpgradeableReadLock upgradeableReadLock = null;
        if (lock instanceof UpgradeableReadLock) {
            upgradeableReadLock = (UpgradeableReadLock) lock;
            upgradeableReadLock.noteWritingStarts();
        }
        VigilantOutputStream vigilantOutputStream = new VigilantOutputStream(outputStream, uri, streamManagement,
                immutableReadFileManagement, upgradeableReadLock, permissions);
        streamManagement.registerStreamGuard(vigilantOutputStream);
        return vigilantOutputStream;
    }

    /**
     * Attempts to get locks on one or more files. The lockability check for
     * multiple URIs can be parallelized because it is independent per URI. This
     * makes sense here (depending on the content of the meta-data files, for
     * example, for year process files of newspapers), there may be cases where
     * several hundred URIs are being requested for locking at the same time.
     * This method is synchronized so that between checking whether the locks
     * can be granted and granting the locks (if successful), no other thread
     * can grant or reset locks that have been taken into account or ignored.
     *
     * @param requests
     *            the desired locks
     * @param rights
     *            existing user rights to be extended
     * @return An object that manages allocated locks or provides information
     *         about conflict originators in case of error.
     * @throws UncheckedIOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    LockingResult tryLock(LockRequests requests, GrantedAccess rights) {
        if (logger.isTraceEnabled()) {
            logger.trace("{} asks the following locks: {}", requests.getUser(), requests.formatLocksAsString());
        }
        Map<URI, Collection<String>> conflictsMap;
        synchronized (this) {
            Stream<Entry<URI, LockingMode>> lockRequests = createStreamOfLockRequests(requests);
            conflictsMap = FutureMapEntry.toMap(checkLockability(requests.getUser(), lockRequests));
            if (conflictsMap.isEmpty()) {
                GrantedAccess access = grantLocks(requests, rights);
                if (logger.isTraceEnabled()) {
                    logger.trace("{} was granted the following locks: {}", requests.getUser(),
                        requests.formatLocksAsString());
                }
                return access;
            } else {
                DeniedAccess noAccess = new DeniedAccess(conflictsMap);
                if (logger.isTraceEnabled()) {
                    logger.trace("{} was denied the requested locks: {}", requests.getUser(), noAccess);
                }
                return noAccess;
            }
        }
    }

    /**
     * Attempts to get locks on one or more files. There are only two results:
     * either, all locks can be granted, or none of the requested locks are
     * granted. In the former case, the conflicts map in the locking result is
     * empty, in the latter case it contains for each conflicting file the users
     * who hold a conflicting lock on the file. If no locks have been granted,
     * the call to {@code close()} on the locking result is meaningless, meaning
     * that leaving the try-with-resources statement will not throw an
     * exception. Just to mention that.
     *
     * @param user
     *            A human-readable string that identifies the user or process
     *            requesting the locks. This string will later be returned to
     *            other users if they try to request a conflicting lock.
     * @param rights
     *            existing user rights to be extended
     * @param requests
     *            the locks to request
     * @return An object that manages allocated locks or provides information
     *         about conflict originators in case of error.
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    public LockingResult tryLock(String user, GrantedAccess rights, Map<URI, LockingMode> requests) throws IOException {
        try {
            return tryLock(new LockRequests(user, requests), rights);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
