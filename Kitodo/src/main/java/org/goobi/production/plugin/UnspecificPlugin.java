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

package org.goobi.production.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.kitodo.exceptions.NotImplementedException;
import org.kitodo.exceptions.PluginException;
import org.kitodo.exceptions.UnreachableCodeException;

/**
 * The class UnspecificPlugin is the base class for a set of redirection classes
 * that take plug-in implementation object as arguments. The plug-in
 * implementation classes can be POJOs that can be compiled without the
 * necessity to link it against the Production code, thus making it possible to
 * provide proprietary plug-ins that do not violate the GPL. The plug-in classes
 * must however implement a number of public methods, which they are checked for
 * upon instantiation. If one of the methods is missing a NoSuchMethodException
 * will be thrown. The methods defined in this class are shared across all
 * plug-ins and must be provided by all of them:
 *
 * <p>
 * <code>void configure(Map)</code><br>
 * The function configure() will be called by the {@link PluginLoader} right
 * after loading a plug-in to pass it the configuration map created in
 * {@link PluginLoader#getPluginConfiguration()}. The plug-in may or may not
 * implement this method.
 * </p>
 *
 * <p>
 * <code>String getDescription(Locale)</code><br>
 * When being called, the function getDescription() shall return a
 * human-readable description for the plug-in. The plug-in may or may not make
 * use of the provided locale to return a description in the named language. If
 * the Locale is null or doesn’t denote a language, the plug-in shall return a
 * human-readable description in English.
 * </p>
 *
 * <p>
 * <code>String getTitle(Locale)</code><br>
 * When being called, the function getTitle() shall return a human-readable name
 * / title for the plug-in. The plug-in may or may not make use of the provided
 * locale to return a title in the named language. If the Locale is null or
 * doesn’t denote a language, the plug-in shall return its human-readable name
 * in English.
 * </p>
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class UnspecificPlugin {
    /**
     * The constant NO_ARGS holds an empty class array.
     */
    private static final Class<?>[] NO_ARGS = new Class<?>[] {};

    /**
     * The field plugin holds a reference to the plug-in implementation class.
     */
    protected final Object plugin;

    /**
     * The field configure holds a Method reference to the method configure() of
     * the plug-in implementation class or <code>null</code> if the plug-in
     * doesn’t implement that method.
     */
    private final Method configure;

    /**
     * The field getTitle holds a Method reference to the method getTitle() of
     * the plug-in implementation class.
     */
    private final Method getTitle;

    /**
     * The field getDescription holds a Method reference to the method
     * getDescription() of the plug-in implementation class.
     */
    private final Method getDescription;

    /**
     * UnspecificPlugin constructor. The abstract class UnspecificPlugin can
     * only be created by classes instantiating it.
     *
     * <p>
     * The constructor saves a reference to the plug-in implementation class in
     * the final field plugin and inspects the class for existence of the
     * methods configure, getDescription and getTitle.
     * </p>
     *
     * @param implementation
     *            plug-in implementation class
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     * @throws NoSuchMethodException
     *             if a required method is not found on the plug-in
     */
    protected UnspecificPlugin(Object implementation) throws NoSuchMethodException {
        plugin = implementation;

        configure = getOptionalMethod("configure", Map.class, Void.TYPE);
        getDescription = getDeclaredMethod("getDescription", Locale.class, String.class);
        getTitle = getDeclaredMethod("getTitle", Locale.class, String.class);
    }

    /**
     * The function create() is called by the {@link PluginLoader} to create a
     * redirection class of the given PluginType to handle a given plug-in
     * implementation class.
     *
     * @param type
     *            type of redirection class required for this implementation
     * @param implementation
     *            plug-in implementation class
     * @return redirection class of the given PluginType
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     * @throws NoSuchMethodException
     *             if a required method is not found on the plugin
     */
    static UnspecificPlugin create(PluginType type, Object implementation) throws NoSuchMethodException {
        switch (type) {
            case IMPORT:
                // return new ImportPlugin(implementation);
                throw new NotImplementedException();
            case CATALOGUE:
                return new CataloguePlugin(implementation);
            default:
                throw new UnreachableCodeException();
        }
    }

    /**
     * The function configure() is called by the {@link PluginLoader} right
     * after loading a plug-in to pass it the configuration map created in
     * {@link PluginLoader#getPluginConfiguration()}. The plug-in may or may not
     * make use of the configuration provided.
     *
     * @param configuration
     *            the configuration map created by the PluginLoader
     */
    void configure(Map<String, String> configuration) {
        if (configure != null) {
            invokeQuietly(plugin, configure, configuration, null);
        }
    }

    /**
     * The function getDeclaredMethod() looks up a no-arg method in the plugin
     * implementation object. It extends
     * {@link java.lang.Class#getDeclaredMethod(String, Class...)} as it also
     * checks the result type.
     *
     * @param name
     *            name of the method to look up
     * @param resultType
     *            result type of the method to look up
     * @return a Method object representing the method implementation in the
     *         plug-in
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     * @throws NoSuchMethodException
     *             if a matching method is not found
     */
    protected final Method getDeclaredMethod(String name, Class<?> resultType) throws NoSuchMethodException {
        return getDeclaredMethod(name, NO_ARGS, resultType);
    }

    /**
     * The function getDeclaredMethod() looks up a 1-arg method in the plugin
     * implementation object. It extends
     * {@link java.lang.Class#getDeclaredMethod(String, Class...)} as it also
     * checks the result type.
     *
     * @param name
     *            name of the method to look up
     * @param parameterType
     *            type of the parameter of the method to look up
     * @param resultType
     *            result type of the method to look up
     * @return a Method object representing the method implementation in the
     *         plug-in
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     * @throws NoSuchMethodException
     *             if a matching method is not found
     */
    protected final Method getDeclaredMethod(String name, Class<?> parameterType, Class<?> resultType)
            throws NoSuchMethodException {
        return getDeclaredMethod(name, new Class[] {parameterType }, resultType);
    }

    /**
     * The function getDeclaredMethod() looks up a method in the plugin
     * implementation object. It extends
     * {@link java.lang.Class#getDeclaredMethod(String, Class...)} as it also
     * checks the result type.
     *
     * @param name
     *            name of the method to look up
     * @param parameterTypes
     *            types of the parameters of the method to look up
     * @param resultType
     *            result type of the method to look up
     * @return a Method object representing the method implementation in the
     *         plug-in
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     * @throws NoSuchMethodException
     *             if a matching method is not found
     */
    protected final Method getDeclaredMethod(String name, Class<?>[] parameterTypes, Class<?> resultType)
            throws NoSuchMethodException {
        Method result = plugin.getClass().getDeclaredMethod(name, parameterTypes);
        if (!resultType.isAssignableFrom(result.getReturnType())) {
            throw new NoSuchMethodException("Bad return type of method " + result.toString() + " ("
                    + Arrays.toString(parameterTypes) + "), must be " + resultType.toString());
        }
        return result;
    }

    /**
     * The function getDescription() returns a human-readable description for
     * the plug-in. The plug-in may or may not make use of the provided locale
     * to return a description in the named language. If the Locale is null or
     * doesn’t denote a language, the plug-in will return a description in
     * English.
     *
     * <p>
     * Currently, this method is not referenced from within the Production code,
     * but this may change in future. The function is provided for completeness
     * since it was part of the old plug-in API, too.
     * </p>
     *
     * @param language
     *            language to get the description in
     * @return a human-readable description for the plug-in
     */
    public String getDescription(Locale language) {
        return invokeQuietly(plugin, getDescription, language, String.class);
    }

    /**
     * The function getDeclaredMethod() looks up a no-arg method in the plug-in
     * implementation object. It extends
     * {@link java.lang.Class#getDeclaredMethod(String, Class...)} as it also
     * checks the result type. If no matching method can be found, it returns
     * null and doesn’t throw a NoSuchMethodException.
     *
     * @param name
     *            name of the method to look up
     * @param resultType
     *            result type of the method to look up
     * @return a Method object representing the method implementation in the
     *         plug-in
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     */
    protected final Method getOptionalMethod(String name, Class<?> resultType) {
        return getOptionalMethod(name, NO_ARGS, resultType);
    }

    /**
     * The function getDeclaredMethod() looks up a 1-arg method in the plugin
     * implementation object. It extends
     * {@link java.lang.Class#getDeclaredMethod(String, Class...)} as it also
     * checks the result type. If no matching method can be found, it returns
     * null and doesn’t throw a NoSuchMethodException.
     *
     * @param name
     *            name of the method to look up
     * @param parameterType
     *            type of the parameter of the method to look up
     * @param resultType
     *            result type of the method to look up
     * @return a Method object representing the method implementation in the
     *         plug-in
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     */
    protected final Method getOptionalMethod(String name, Class<?> parameterType, Class<?> resultType) {
        return getOptionalMethod(name, new Class[] {parameterType }, resultType);
    }

    /**
     * The function getOptionalMethod() looks up a method in the plug-in
     * implementation object. It extends
     * {@link java.lang.Class#getDeclaredMethod(String, Class...)} as it also
     * checks the result type. If no matching method can be found, it returns
     * null and doesn’t throw a NoSuchMethodException.
     *
     * @param name
     *            name of the method to look up
     * @param parameterTypes
     *            types of the parameters of the method to look up
     * @param resultType
     *            result type of the method to look up
     * @return a Method object representing the method implementation in the
     *         plug-in
     * @throws SecurityException
     *             If a security manager, is present and an invocation of its
     *             method checkMemberAccess(this, Member.DECLARED) denies access
     *             to the declared method or if the caller’s class loader is not
     *             the same as or an ancestor of the class loader for the
     *             current class and invocation of the security manager’s
     *             checkPackageAccess() denies access to the package of this
     *             class.
     */
    protected final Method getOptionalMethod(String name, Class<?>[] parameterTypes, Class<?> resultType) {
        try {
            return getDeclaredMethod(name, parameterTypes, resultType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * The function getTitle() returns a human-readable name title for the
     * plug-in. The plug-in may or may not make use of the provided locale to
     * return a title in the named language. If the Locale is null or doesn’t
     * denote a language, the plug-in will return its English name.
     *
     * @param language
     *            language to get the plugin’s name in
     * @return a human-readable name for the plug-in
     */
    public String getTitle(Locale language) {
        return invokeQuietly(plugin, getTitle, language, String.class);
    }

    /**
     * The method getType() must be implemented in the redirection class that
     * implements this abstract class. It must return the PluginType
     * corresponding to that class.
     *
     * @return a value from enum PluginType
     */
    public abstract PluginType getType();

    /**
     * The function invokeQuietly() invokes a no-arg method on a given object,
     * returning a result of the given resultType. RuntimeExceptions wrapped as
     * InvocationTargetException by Method.invoke() will be unwrapped, any other
     * wrapped exceptions will be unwrapped and rewrapped as RuntimeExceptions,
     * the same is true for any other checked exceptions that may occur.
     *
     * @param object
     *            object to invoke a method on
     * @param method
     *            method to invoke on the object
     * @param resultType
     *            result type of the method to invoke (may be Void.TYPE)
     * @return an object of the specified type
     */
    protected <T> T invokeQuietly(Object object, Method method, Class<T> resultType) {
        return invokeQuietly(object, method, NO_ARGS, resultType);
    }

    /**
     * The function invokeQuietly() invokes a method on a given object, passing
     * the argument provided and returning a result of the given resultType.
     * RuntimeExceptions wrapped as InvocationTargetException by Method.invoke()
     * will be unwrapped, any other wrapped exceptions will be unwrapped and
     * rewrapped as RuntimeExceptions, the same is true for any other checked
     * exceptions that may occur.
     *
     * @param object
     *            object to invoke a method on
     * @param method
     *            method to invoke on the object
     * @param arg
     *            argument to pass to the object
     * @param resultType
     *            result type of the method to invoke (may be Void.TYPE)
     * @return an object of the specified type
     */
    protected <T> T invokeQuietly(Object object, Method method, Object arg, Class<T> resultType) {
        return invokeQuietly(object, method, new Object[] {arg }, resultType);
    }

    /**
     * The function invokeQuietly() invokes a method on a given object, passing
     * the arguments provided and returning a result of the given resultType.
     * RuntimeExceptions wrapped as InvocationTargetException by Method.invoke()
     * will be unwrapped, any other wrapped exceptions will be unwrapped and
     * rewrapped as RuntimeExceptions, the same is true for any other checked
     * exceptions that may occur.
     *
     * @param object
     *            object to invoke a method on
     * @param method
     *            method to invoke on the object
     * @param args
     *            arguments to pass to the object
     * @param resultType
     *            result type of the method to invoke (may be Void.TYPE)
     * @return an object of the specified type
     */
    @SuppressWarnings("unchecked")
    protected <T> T invokeQuietly(Object object, Method method, Object[] args, Class<T> resultType) {
        try {
            return (T) method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PluginException(e.getMessage(), e);
        }
    }

    /**
     * The function typeOf() returns the PluginType of the given plugin class.
     *
     * <p>
     * When changing the further plugin API to the new format, add the
     * </p>
     *
     * @param clazz
     *            the class to inspect
     * @return a value from enum PluginType, or null if there is no match
     */
    public static PluginType typeOf(Class<? extends UnspecificPlugin> clazz) {
        // if (ImportPlugin.class.isAssignableFrom(clazz))
        // return PluginType.Import;
        // if (CommandPlugin.class.isAssignableFrom(clazz))
        // return PluginType.Command;
        if (CataloguePlugin.class.isAssignableFrom(clazz)) {
            return PluginType.CATALOGUE;
        }
        return null;
    }
}
