package de.unibonn.iai.eis.diachron.webinterface.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Calse de ayuda para JSF
 * @author manrique_alejandro
 * @version 1.0, 27/08/2009
 */
public class UtilFaces {

    // Bundles de la aplicacion
    /** */
    public static final String MESSAGE_BUNDLE = "MessageResources";

    /**
     * Metodo que obtiene el classloader actualmente ejecutandose
     * @param defaultObject El objeto a tomar el classloader
     * @return El classloader
     */
    protected static ClassLoader getCurrentClassLoader(Object defaultObject) {
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	if (loader == null) {
	    loader = defaultObject.getClass().getClassLoader();
	}
	return loader;
    }

    /**
     * Metodo que obtiene el contexto de faces
     * @return El contexto de faces
     */
    public static FacesContext getFacesContext() {
	return FacesContext.getCurrentInstance();
    }

    /**
     * Metodo que obtiene el contexto de servlets
     * @return El contexto de servlets
     */
    public static ServletContext getServletContext() {
	FacesContext facesContext = getFacesContext();
	return (ServletContext) facesContext.getExternalContext().getContext();
    }

    /**
     * Metodo que obtiene el request del servlet de JSF
     * @return El request del servlet de JSF
     */
    public static HttpServletRequest getRequest() {
	FacesContext facesContext = getFacesContext();
	return (HttpServletRequest) facesContext.getExternalContext().getRequest();
    }

    /**
     * Metodo que obtiene el response del servlet de JSF
     * @return El response del servlet de JSF
     */
    public static HttpServletResponse getResponse() {
	FacesContext facesContext = getFacesContext();
	return (HttpServletResponse) facesContext.getExternalContext().getResponse();
    }

    /**
     * Metodo que obtiene la sesion de HTTP del servidor
     * @return La sesion de HTTP del servidor
     */
    public static HttpSession getSession() {
	HttpServletRequest request = getRequest();
	return request.getSession();
    }

    /**
     * Metodo que obtiene el atributo del request
     * @param attributeName El nombre del atributo
     * @return El objeto obtenido del request
     */
    public static Object getRequestAttribute(String attributeName) {
	HttpServletRequest request = getRequest();
	return request.getAttribute(attributeName);
    }

    /**
     * Metodo que setea el atributo del request
     * @param attributeName El nombre del atriuto
     * @param object El objeto a setear
     */
    public static void setRequestAttribute(String attributeName, Object object) {
	HttpServletRequest request = getRequest();
	request.setAttribute(attributeName, object);
    }

    /**
     * Metodo que obtiene el atributo de la sesion
     * @param attributeName El nombre del atributo
     * @return El objeto del atributo de sesion
     */
    public static Object getSessionAttribute(String attributeName) {
	HttpSession session = getSession();
	return session.getAttribute(attributeName);
    }

    /**
     * Metodo que setea el atributo de la sesion
     * @param attributeName El nombre del atributo
     * @param object El objeto del atributo a setear
     */
    public static void setSessionAttribute(String attributeName, Object object) {
	HttpSession session = getSession();
	session.setAttribute(attributeName, object);
    }

    /**
     * Metodo que obtiene un atributo primero del request y luego de la sesion
     * @param attributeName El nombre del atributo
     * @return El objeto del atributo
     */
    public static Object getAttribute(String attributeName) {
	Object obj = getRequestAttribute(attributeName);
	if (obj == null) {
	    obj = getSessionAttribute(attributeName);
	}
	return obj;
    }

    /**
     * Metodo que obtiene los parametros del request
     * @param paramName El nombre del parametro
     * @return El valor del parametro
     */
    public static Object getRequestParameter(String paramName) {
	HttpServletRequest request = getRequest();
	return request.getParameter(paramName);
    }

    /**
     * Metodo que obtiene el atributo de la accion
     * @param event El evento
     * @param name El nombre del atributo
     * @return El valor del atributo de la accion
     */
    public static Object getActionAttribute(ActionEvent event, String name) {
	return event.getComponent().getAttributes().get(name);
    }

    /**
     * Metodo que obtiene el backinBean de JSF
     * @param backinBeanName El nombre del backinBean
     * @return El backinBean de la pagina JSF
     */
    public static Object getBackinBean(String backinBeanName) {
	FacesContext facesContext = getFacesContext();
	return facesContext.getExternalContext().getSessionMap().get(backinBeanName);
    }

    /**
     * Metodo que obtiene el valor del mapa de parametros de request
     * @param parameter El parametro a obtener
     * @return El valor del parametro a obtener
     */
    public static Object getRequestParameterMap(String parameter) {
	FacesContext facesContext = getFacesContext();
	return facesContext.getExternalContext().getRequestParameterMap().get(parameter);
    }

    /**
     * Metodo que agrega un mensaje a JSF para su presentacion
     * @param name El nombre del mensaje
     * @param message El mensaje
     */
    public static void addFacesMessage(String name, FacesMessage message) {
	FacesContext facesContext = getFacesContext();
	facesContext.addMessage(name, message);
    }

    /**
     * Metodo que obtiene el bundle de propiedades
     * @param bundleName El nombre del bundle de propiedades
     * @return El bundle de propiedades
     */
    protected static ResourceBundle getResourceBundle(String bundleName) {
	if (bundleName == null) {
	    return null;
	}
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	return ResourceBundle.getBundle(bundleName, getLocale(), loader);
    }

    /**
     * Metodo que obtiene el bundle de mensajes
     * @return El bundle de mensajes
     */
    protected static ResourceBundle getMessageResourceBundle() {
	FacesContext context = getFacesContext();
	return getResourceBundle(context.getApplication().getMessageBundle());
    }

    /**
     * Metodo que obtiene el mensaje de un bundle y setea sus parametros
     * @param bundle El bundle a utlizar
     * @param key La llave dentro del bundle
     * @param params Los parametros para el mensaje
     * @param locale Si se va a utilizar un locale
     * @return El mensaje obtenido
     */
    public static String getMessageResource(ResourceBundle bundle, String key, Object params[], Locale locale) {
	String text = null;
	if (locale == null) {
	    locale = getLocale();
	}
	try {
	    text = bundle.getString(key);
	} catch (MissingResourceException e) {
	    text = "?? key " + key + " not found ??";
	}
	text = getMessageResource(text, params);
	return text;
    }

    /**
     * Metodo que obtiene el mensaje seteando sus parametros. Si el mensaje es: Esto es un {1} {0}
     * y se pasa como parametros [interno, error] el mensaje queda: Esto es un error interno
     * @param text El texto del mensaje
     * @param params El parametro de mensaje
     * @return El mensaje completo
     */
    public static String getMessageResource(String text, Object params[]) {
	if (params != null) {
	    MessageFormat mf = new MessageFormat(text);
	    text = mf.format(params, new StringBuffer(), null).toString();
	}
	return text;
    }

    /**
     * Metodo que obtiene el mensaje seteando sus parametros.  
     * @param bundleName El nombre del bundle a utilizar
     * @param key La llave a buscar
     * @param params Los parametros del mensaje
     * @return El mensaje completo
     * @see UtilFaces#getMessageResource(String, Object[])
     */
    public static String getMessageResource(String bundleName, String key, Object params[]) {
	ResourceBundle bundle = getResourceBundle(bundleName);
	return getMessageResource(bundle, key, params, null);
    }

    /**
     * Metodo que devuelve un conjunto de mensajes del bundle
     * @param bundle El bundle a utilizar
     * @param key La llave a buscar
     * @return La lista de mensajes que coincidan con esa llave
     */
    public static String[] getArrayMessageResource(ResourceBundle bundle, String key) {
	String[] text = null;
	try {
	    text = bundle.getStringArray(key);
	} catch (MissingResourceException e) {
	    text = new String[1];
	    text[0] = "?? key " + key + " not found ??";
	}
	return text;
    }

    /**
     * Metodo que devuelve un conjunto de mensajes del bundle
     * @param bundleName El nombre del bundle
     * @param key La llave a utilizar
     * @return La lista de mensajes que coincidan con esa llave
     */
    public static String[] getArrayMessageResource(String bundleName, String key) {
	ResourceBundle bundle = getResourceBundle(bundleName);
	return getArrayMessageResource(bundle, key);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo informacion
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addInfoMessage(String key, Object params[]) {
	addMessage(null, FacesMessage.SEVERITY_INFO, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo informacion a un componente
     * @param target El componente JSF objetivo
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addInfoMessage(String target, String key, Object params[]) {
	addMessage(target, FacesMessage.SEVERITY_INFO, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo error
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addErrorMessage(String key, Object params[]) {
	addMessage(null, FacesMessage.SEVERITY_ERROR, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo error a un componente
     * @param target El componente JSF objetivo
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addErrorMessage(String target, String key, Object params[]) {
	addMessage(target, FacesMessage.SEVERITY_ERROR, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo advertencia
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addWarnMessage(String key, Object params[]) {
	addMessage(null, FacesMessage.SEVERITY_WARN, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo advertencia a un componente
     * @param target El componente JSF objetivo
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addWarnMessage(String target, String key, Object params[]) {
	addMessage(target, FacesMessage.SEVERITY_WARN, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo fatal
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addFatalMessage(String key, Object params[]) {
	addMessage(null, FacesMessage.SEVERITY_FATAL, key, params);
    }

    /**
     * Metodo que agrega mensajes JSF de tipo fatal a un componente
     * @param target El componente JSF objetivo
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addFatalMessage(String target, String key, Object params[]) {
	addMessage(target, FacesMessage.SEVERITY_FATAL, key, params);
    }

    /**
     * Metodo que agrega un mensaje a JSF de los distintos bundles que utiliza la aplicacion de convocatoria
     * @param target El componente JSF objetivo
     * @param severity La severidad del mensaje
     * @param key La llave del mensaje
     * @param params Los parametros del mensaje
     */
    public static void addMessage(String target, FacesMessage.Severity severity, String key, Object params[]) {
	String text = UtilFaces.getMessageResource(getMessageResourceBundle(), key, params, getLocale());
	if (text == null) {
	    text = UtilFaces.getMessageResource(getResourceBundle(MESSAGE_BUNDLE), key, params, getLocale());
	}	
	FacesMessage fm = new FacesMessage(severity, text, text);
	UtilFaces.addFacesMessage(target, fm);
    }

    /**
     * Metodo que obtiene el locale actualmente utilizado en la aplicacion JSF
     * @return El locale de la aplicacion JSF
     */
    public static Locale getLocale() {
	try {
	    FacesContext context = getFacesContext();
	    return context.getViewRoot().getLocale();
	} catch (Exception ex) {
	    // No hacer nada
	}
	return new Locale("es");
    }

    /**
     * Metodo que crea una value expresion para ser utilizada por los bindings de JSF
     * @param valueExpression El value expresion
     * @param valueType La clase del value expresion
     * @return El value expresion a usar
     */
    public static ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
	FacesContext facesContext = getFacesContext();
	return facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
		valueExpression, valueType);
    }

    /**
     * Metodo que duelve la direccion ip del cliente que invoco la pagina
     * 
     * @return La direcci�n ip del cliente que invoco la pagina
     */
    public static String getClientIpAddress() {
	return getRequest().getRemoteAddr();
    }

}
