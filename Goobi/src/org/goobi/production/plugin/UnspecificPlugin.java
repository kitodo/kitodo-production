package org.goobi.production.plugin;

import java.lang.reflect.Method;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;

import com.sharkysoft.util.NotImplementedException;
import com.sharkysoft.util.UnreachableCodeException;

public abstract class UnspecificPlugin {

	protected static final Class<?>[] NO_ARGS = new Class<?>[] {};

	protected final Object plugin;
	private final Method getTitle;
	private final Method getDescription;

	public UnspecificPlugin(Object implementation) throws SecurityException, NoSuchMethodException {
		plugin = implementation;
		getDescription = getDeclaredMethod("getDescription", NO_ARGS, String.class);
		getTitle = getDeclaredMethod("getTitle", NO_ARGS, String.class);
	}

	public static UnspecificPlugin create(PluginType type, Object implementation) throws SecurityException,
			NoSuchMethodException {
		switch (type) {
		case Command:
			// return new CommandPlugin(implementation);
			throw new NotImplementedException();
		case Import:
			// return new ImportPlugin(implementation);
			throw new NotImplementedException();
		case Opac:
			return new CataloguePlugin(implementation);
		case Step:
			// return new StepPlugin(implementation);
			throw new NotImplementedException();
		case Validation:
			// return new ValidationPlugin(implementation);
			throw new NotImplementedException();
		default:
			throw new UnreachableCodeException();
		}
	}

	protected final Method getDeclaredMethod(String name, Class<?>[] parameterTypes, Class<?> resultType)
			throws SecurityException, NoSuchMethodException {
		Method result = plugin.getClass().getDeclaredMethod(name, parameterTypes);
		if (!resultType.isAssignableFrom(result.getReturnType()))
			throw new NoSuchMethodException("Bad return type of method " + result.toString() + " ("
					+ parameterTypes.toString() + "), must be " + resultType.toString());
		return result;
	}

	public String getDescription() {
		return invokeQuietly(plugin, getDescription, String.class);
	}

	public String getTitle() {
		return invokeQuietly(plugin, getTitle, String.class);
	}

	public abstract PluginType getType();

	protected <T> T invokeQuietly(Object object, Method method, Class<T> resultType) {
		return invokeQuietly(object, method, NO_ARGS, resultType);
	}

	protected <T> T invokeQuietly(Object object, Method method, Object arg0, Class<T> resultType) {
		return invokeQuietly(object, method, new Object[] { arg0 }, resultType);
	}

	@SuppressWarnings("unchecked")
	protected <T> T invokeQuietly(Object object, Method method, Object[] args, Class<T> resultType) {
		try {
			return (T) method.invoke(object, args);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static PluginType typeOf(Class<? extends UnspecificPlugin> type) {
		// if (ImportPlugin.class.isAssignableFrom(type))
		//     return PluginType.Import;
		// if (StepPlugin.class.isAssignableFrom(type))
		//     return PluginType.Step;
		// if (ValidationPlugin.class.isAssignableFrom(type))
		//     return PluginType.Validation;
		// if (CommandPlugin.class.isAssignableFrom(type))
		//     return PluginType.Command;
		if (CataloguePlugin.class.isAssignableFrom(type))
			return PluginType.Opac;
		return null;
	}
}
