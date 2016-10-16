/*
 Copyright (c) 2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package se.krka.kahlua.converter;

import java.util.HashMap;
import java.util.Map;

public class KahluaConverterManager {

	@SuppressWarnings("unchecked")
	private static final Map<Class, Class> PRIMITIVE_CLASS = new HashMap<Class, Class>();
	static {
		PRIMITIVE_CLASS.put(boolean.class, Boolean.class);
		PRIMITIVE_CLASS.put(byte.class, Byte.class);
		PRIMITIVE_CLASS.put(char.class, Character.class);
		PRIMITIVE_CLASS.put(short.class, short.class);
		PRIMITIVE_CLASS.put(int.class, Integer.class);
		PRIMITIVE_CLASS.put(long.class, Long.class);
		PRIMITIVE_CLASS.put(float.class, Float.class);
		PRIMITIVE_CLASS.put(double.class, Double.class);
	}
	
	@SuppressWarnings("unchecked")
	private static final Map<Class, LuaToJavaConverter> LUA_NULL_MAP = new HashMap<Class, LuaToJavaConverter>();
	@SuppressWarnings("unchecked")
	private final Map<Class, Map<Class, LuaToJavaConverter>> luaToJava = new HashMap<Class, Map<Class,LuaToJavaConverter>>();
	@SuppressWarnings("unchecked")
	private final Map<Class, Map<Class, LuaToJavaConverter>> luatoJavaCache = new HashMap<Class, Map<Class,LuaToJavaConverter>>();


	@SuppressWarnings("unchecked")
	private static final JavaToLuaConverter NULL_CONVERTER = new JavaToLuaConverter<Object>() {
		public Object fromJavaToLua(Object javaObject) {
			return null;
		}

		public Class<Object> getJavaType() {
			return Object.class;
		}
		
	};
	@SuppressWarnings("unchecked")
	private final Map<Class, JavaToLuaConverter> javaToLua = new HashMap<Class, JavaToLuaConverter>();
	@SuppressWarnings("unchecked")
	private final Map<Class, JavaToLuaConverter> javaToLuaCache = new HashMap<Class, JavaToLuaConverter>();
	
	public KahluaConverterManager() {
	}

    @SuppressWarnings("unchecked")
	public void addLuaConverter(LuaToJavaConverter converter) {
		Map<Class, LuaToJavaConverter> map = getOrCreate(luaToJava, converter.getLuaType());
        Class javaType = converter.getJavaType();
        LuaToJavaConverter current = map.get(javaType);
        if (current != null) {
            if (current instanceof MultiLuaToJavaConverter) {
                ((MultiLuaToJavaConverter) current).add(converter);
            } else {
                MultiLuaToJavaConverter multiLuaToJavaConverter = new MultiLuaToJavaConverter(converter.getLuaType(), javaType);
                multiLuaToJavaConverter.add(current);
                multiLuaToJavaConverter.add(converter);
                map.put(javaType, multiLuaToJavaConverter);
            }
        } else {
            map.put(javaType, converter);
        }
		luatoJavaCache.clear();
	}

	@SuppressWarnings("unchecked")
	public void addJavaConverter(JavaToLuaConverter converter) {
        Class javaType = converter.getJavaType();
        JavaToLuaConverter current = javaToLua.get(javaType);
        if (current != null) {
            if (current instanceof MultiJavaToLuaConverter) {
                ((MultiJavaToLuaConverter) current).add(converter);
            } else {
                MultiJavaToLuaConverter multiConverter = new MultiJavaToLuaConverter(javaType);
                multiConverter.add(current);
                multiConverter.add(converter);
                javaToLua.put(javaType, multiConverter);
            }

        } else {
            javaToLua.put(javaType, converter);
        }
		javaToLuaCache.clear();
	}

	@SuppressWarnings("unchecked")
	private Map<Class, LuaToJavaConverter> getOrCreate(Map<Class, Map<Class, LuaToJavaConverter>> luaToJava2, Class luaType) {
		Map<Class, LuaToJavaConverter> map = luaToJava2.get(luaType);
		if (map == null) {
			map = new HashMap<Class, LuaToJavaConverter>();
			luaToJava2.put(luaType, map);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public <T> T fromLuaToJava(Object luaObject, Class<T> javaClass) {
		if (luaObject == null) {
			return null;
		}
		
		if (javaClass.isPrimitive()) {
			javaClass = PRIMITIVE_CLASS.get(javaClass);
		}
		
		if (javaClass.isInstance(luaObject)) {
			return (T) luaObject;
		}
		
		Class<?> luaClass = luaObject.getClass();
		Map<Class, LuaToJavaConverter> map = getLuaCache(luaClass);

        Class<?> scanClass = javaClass;
        while (scanClass != null) {
            LuaToJavaConverter converter = map.get(scanClass);
            if (converter != null) {
                Object o = converter.fromLuaToJava(luaObject, javaClass);
                if (o != null) {
                    return (T) o;
                }
            }
            scanClass = scanClass.getSuperclass();
        }
        return tryInterfaces(map, javaClass, luaObject);
	}

    private <T> T tryInterfaces(Map<Class, LuaToJavaConverter> map, Class<T> javaClass, Object luaObject) {
        if (javaClass == null) {
            return null;
        }
        LuaToJavaConverter converter = map.get(javaClass);
        if (converter != null) {
            Object o = converter.fromLuaToJava(luaObject, javaClass);
            if (o != null) {
                return (T) o;
            }
        }
        for (Class<?> aClass : javaClass.getInterfaces()) {
            Object o = tryInterfaces(map, aClass, luaObject);
            if (o != null) {
                return (T) o;
            }
        }
        return (T) tryInterfaces(map, javaClass.getSuperclass(), luaObject);
    }

    @SuppressWarnings("unchecked")
	private Map<Class, LuaToJavaConverter> createLuaCache(Class<?> luaClass) {
		HashMap<Class, LuaToJavaConverter> map = new HashMap<Class, LuaToJavaConverter>();
		luatoJavaCache.put(luaClass, map);
		
		map.putAll(getLuaCache(luaClass.getSuperclass()));
		for (Class clazz: luaClass.getInterfaces()) {
			map.putAll(getLuaCache(clazz));
		}
		Map<Class, LuaToJavaConverter> directMap = luaToJava.get(luaClass);
		if (directMap != null) {
			map.putAll(directMap);
		}
		
		return map;
	}

	@SuppressWarnings("unchecked")
	private Map<Class, LuaToJavaConverter> getLuaCache(Class<?> clazz) {
		if (clazz == null) {
			return LUA_NULL_MAP;
		}
		Map<Class, LuaToJavaConverter> map = luatoJavaCache.get(clazz);
		if (map == null) {
			map = createLuaCache(clazz);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Object fromJavaToLua(Object javaObject) {
		if (javaObject == null) {
			return null;
		}
		Class clazz = javaObject.getClass();
		JavaToLuaConverter converter = getJavaCache(clazz);
		try {
			Object o = converter.fromJavaToLua(javaObject);
			if (o == null) {
				return javaObject;
			}
			return o;
		} catch (StackOverflowError e) {
			throw new RuntimeException("Could not convert " + javaObject + ": it contained recursive elements.");
		}
	}

	@SuppressWarnings("unchecked")
	private JavaToLuaConverter getJavaCache(Class clazz) {
		if (clazz == null) {
			return NULL_CONVERTER;
		}
		JavaToLuaConverter converter = javaToLuaCache.get(clazz);
		if (converter == null) {
			converter = createJavaCache(clazz);
		}
		javaToLuaCache.put(clazz, converter);
		return converter;
	}

	@SuppressWarnings("unchecked")
	private JavaToLuaConverter createJavaCache(Class javaClass) {
		JavaToLuaConverter converter = javaToLua.get(javaClass);
		if (converter != null) {
			return converter;
		}
		for (Class clazz: javaClass.getInterfaces()) {
			converter = getJavaCache(clazz);
			if (converter != NULL_CONVERTER) {
				return converter;
			}
		}
		return getJavaCache(javaClass.getSuperclass());
	}
}
