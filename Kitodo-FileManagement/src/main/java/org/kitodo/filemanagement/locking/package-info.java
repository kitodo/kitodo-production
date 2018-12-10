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
/**
 * This package implements an incredibly complicated system of locks for mutual
 * parallel access to meta-data files. This system became necessary because it
 * is desired to keep the meta-data in the file format METS. METS is an XML
 * format and defines that references to other linked files be located in the
 * file. So they can not just be written to the database, and this is where the
 * problem starts. This in turn means that for certain accesses, several METS
 * files must be read and, if necessary, also written, and this in turn can lead
 * to problems if several users in the front-end simultaneously work on
 * interrelated METS files. Locks have been implemented to prevent users from
 * overwriting each other’s changes, but, at the same time, giving as many users
 * as possible simultaneous access to related METS files.
 * 
 * <p>
 * There are a total of four types of locks that are defined in the
 * {@linkplain org.kitodo.api.filemanagement.LockingMode} enumeration in the
 * API:
 * <ul>
 * <li>The most easily understood type of lock is {@code EXCLUSIVE} access. Here
 * a user has all rights to read and write a file. At the same time, no other
 * user can read the file, let alone write it. This is a good choice if it is
 * the file that the user is currently editing. Technically, it should be noted
 * here that the lock may only be released once the user has closed all read and
 * write channels to the file. So all open connections must be monitored. Of
 * course, this also requires the fairness that the using code actually reads or
 * writes the URI only through the management module, and does not ignore it,
 * but we'll go over that at this point.
 * <li>The permission to {@code IMMUTABLE_READ} is a bit the opposite of it.
 * Here, the user gets a separate copy of the file, which he can read over and
 * over again, but which does not change as long as the lock is maintained, even
 * if another user writes around in the file in the meantime. For this purpose,
 * a temporary copy is simply created, to which the user then gets read access
 * as long as and as often as he wants. It could certainly be made even more
 * charming, with a special InputStream, which writes the copy of the file only
 * when it is actually read, but for the beginning that was first realized so.
 * Since we are talking about files in the kilobyte range that should not be a
 * problem. The thing is, though, that the user can no longer write to this file
 * if the underlying main file has changed. This access is best for files that
 * do not need to be changed in the current editing process. This is especially
 * true for files of parent entities.
 * <li>Then there is an {@code UPGRADEABLE_READ} lock. This is a lock on files
 * that typically only need to be read, but maybe just need to be changed. This
 * is true for child units if the actual edited file is parent of it. Usually
 * you just have to read them, but if the link is changed, it just needs to be
 * changed as well. No copy of the file is created here, but at the same time no
 * exclusive access to the file can be set up by another user. However, other
 * users can also read the file at the same time.
 * <li>For an {@code UPGRADEABLE_READ} lock, an {@code UPGRADE_WRITE_ONCE} can
 * be requested. You have to do that in case you have to edit the file. The
 * contract condition here is that the file is first re-imported after the
 * upgrade has been granted, as another process could have received and used
 * such an upgrade in the meantime, so the file can now have been changed. The
 * upgrade can only be granted if no other user currently has an open read
 * channel on the file. At this point, we assume that the reading and the
 * subsequent writing process take place within a few seconds. During this time,
 * other users will not be able to open a read channel to the file, such
 * requests will hang, so this should actually happen in a timely manner.
 * </ul>
 * 
 * <p>
 * Of course, you can also request other locks at any time, and if nothing
 * speaks against it, it is also granted, but for the optimal operation of the
 * application, you should do it as described above. Of course, for all locks,
 * the program should be written so that if the user stops responding for an
 * extended period of time, the user logs out of the system and returns the
 * lock. But this is to organize elsewhere, we assume here that this happens.
 * 
 * <p>
 * A few words to return locks: Because properly returning locks is critical to
 * meaningful multi-user operation of the system, unlocking has been implemented
 * so that {@linkplain java.lang.AutoCloseable} can be used. Meaning, if the
 * lock is requested in a try-with-resources statement, the lock is released at
 * the end of the block, even if an exception occurs within the code. Awesome,
 * isn’t it? Of course, this works only if requesting read or write channels
 * within the block also happens in try-with-resources statements, but you
 * should always do that anyway so that the application does not leak.
 * 
 */
package org.kitodo.filemanagement.locking;
