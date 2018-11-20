package com.papaya.protocol.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Serialized tool classes based on {@code Protostuff}
 * 
 * @see Objenesis
 * @author shellpo shih
 * @version 1.0
 */
public class Serializations {

	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
	private static Objenesis objenesis = new ObjenesisStd(true);

	private Serializations() {
	}

	/**
	 * Serialize the object data and convert it into {@code Byte} array.
	 * 
	 * @param message
	 *            java {@code Object}
	 * @return {@code Byte} array
	 */
	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T message) {
		Class<T> cls = (Class<T>) message.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			Schema<T> schema = schema(cls);
			return ProtostuffIOUtil.toByteArray(message, schema, buffer);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

	/**
	 * Deserialize objects to convert byte arrays into Java objects.
	 * 
	 * @param data
	 *            {@code Byte} array
	 * @param clazz
	 *            Class Scheme
	 * @return {@code T} java {@code Object}
	 */
	public static <T> T deserialize(byte[] data, Class<T> clazz) {
		try {
			T message = (T) objenesis.newInstance(clazz);
			Schema<T> schema = schema(clazz);
			ProtostuffIOUtil.mergeFrom(data, message, schema);
			return message;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/* ============tools============== */
	/**
	 * Serialize the object data and convert it into {@code Byte} array.
	 * 
	 * @param message
	 *            java {@code Object}
	 * @return {@code Byte} array
	 */
	@SuppressWarnings("unchecked")
	private static <T> Schema<T> schema(Class<T> message) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(message);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(message);
			if (schema != null) {
				cachedSchema.put(message, schema);
			}
		}
		return schema;
	}
}
