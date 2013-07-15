package test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;

public class PrivateAccessor {
    
    /**
    * Get a private object
    * @param obj - Object to get private attribute of
    * @param fieldName - Field name of attribute
    * @return Object reference to private attribute 
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object obj, String fieldName) {
           Assert.assertNotNull(obj);
           Assert.assertNotNull(fieldName);
           
           // Find the field
           final Field fields[] = obj.getClass().getDeclaredFields();
           int i=0;
           while(i < fields.length && !fields[i].getName().equals(fieldName)) { i++; }
           if(i < fields.length) {
                  fields[i].setAccessible(true);
                  try {
                        return (T)fields[i].get(obj);
                  } catch (IllegalArgumentException e) {
                        Assert.fail("Somehow the field name has disappeared after we found it");
                  } catch (IllegalAccessException e) {
                        Assert.fail("Something has gone wrong with the field access workaround");
                  }                    
           } else {
                  Assert.fail("Missing field " + fieldName);
           }
           return null;
    }
    
    /**
    * Allows the invocation of a private method
    * @param obj - Object the method belongs to
    * @param methodName - Name of the method
    * @param params - Array of parameters to pass to the method
    * @return
    */
    public static Object invokePrivateMethod(Object obj, String methodName, Object[] params) {
           Assert.assertNotNull(obj);
           Assert.assertNotNull(methodName);
           
           // Find the method         
           final Method methods[] = obj.getClass().getDeclaredMethods();
           int i=0;
           while(i < methods.length && !methods[i].getName().equals(methodName)) { i++; }
           if(i < methods.length) {
                  methods[i].setAccessible(true);
                  try {
                        return methods[i].invoke(obj, params);
                  } catch (IllegalAccessException e) {
                        Assert.fail("Something has gone wrong with the method access workaround");
                  } catch (InvocationTargetException e) {
                        Assert.fail("Problem invoking private method " + methodName);
                  }                    
           } else {
                  Assert.fail("Missing method " + methodName);
           }
           return null;
    }
    
    /**
    * Allows the invocation of a private constructor
    * @param o - Object the constructor is for
    * @param constructorName - Name of the method
    * @param params - Array of parameters to pass to the method
    * @return
    */
    public static Object invokePrivateConstructor(String className, Object[] params) {
           Assert.assertNotNull(className);
           
           Class<?> c;
           try {
                  c = Class.forName(className);
           } catch (ClassNotFoundException e1) {
                  Assert.fail("Cannot find class to constuct " + className + ", Stack:" + e1.toString());
                  return null; // Never get here, silly eclipse
           }
           
           // Find the constructor           
           final Constructor<?> constructors[] = c.getDeclaredConstructors();
           int i=0;
           boolean found = false;
           while(i < constructors.length && !found) {
                  Class<?>[] thisParams = constructors[i].getParameterTypes();
                  found=thisParams.length==params.length;
                  int j=0; 
                  while(j < thisParams.length && found) {
                        found = thisParams[j].getName().equals(params[j].getClass().getName());
                        j++;
                  }
                  if(!found) i++; 
           }
           if(i < constructors.length) {
                  constructors[i].setAccessible(true);
                  try {
                        return constructors[i].newInstance(params);
                  } catch (IllegalAccessException e) {
                        Assert.fail("Something has gone wrong with the method access workaround");
                  } catch (IllegalArgumentException e) {
                        Assert.fail("Somehow the constructor has disappeared after we found it");
                  } catch (InstantiationException e) {
                        Assert.fail("Issue with instantiation of private constructor");
                  } catch (InvocationTargetException e) {
                        Assert.fail(e.toString());
                  }                    
           } else {
                  Assert.fail("Missing constuctor");
           }
           return null;
    }
}
