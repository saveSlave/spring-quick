package com.saveslave.commons.template;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @ClassName: RedisRepository
 * @Description: Redis Repository redis 基本操作 可扩展,基本够用了
 *
 */
@Slf4j
public class StringRedisRepository extends RedisTemplate<String, String> {
	/**
	 * 默认编码
	 */
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * key序列化
	 */
	private static final StringRedisSerializer STRING_SERIALIZER = new StringRedisSerializer();



	public StringRedisRepository() {
		this.setKeySerializer(STRING_SERIALIZER);
		this.setValueSerializer(STRING_SERIALIZER);
	}

	/**
	 * 清空DB
	 *
	 * @param node redis 节点
	 */
	public void flushDB(RedisClusterNode node) {
		this.opsForCluster().flushDb(node);
	}

	/**
	 * 添加到带有 过期时间的 缓存
	 *
	 * @param key   redis主键
	 * @param value 值
	 * @param time  过期时间(单位秒)
	 */
	public void setExpire(final byte[] key, final byte[] value, final long time) {
		this.execute((RedisCallback<Long>) connection -> {
			connection.setEx(key, time, value);
			log.debug("[redisTemplate redis]放入 缓存  url:{} ========缓存时间为{}秒", key, time);
			return 1L;
		});
		/*RedisConnection redisConnection = this.getConnectionFactory().getConnection();
		try{
			redisConnection.setEx(key,time,value);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			redisConnection.close();
		}*/

		/*this.execute((RedisCallback<Long>) connection -> {
			connection.setEx(key, time, value);
			log.debug("[redisTemplate redis]放入 缓存  url:{} ========缓存时间为{}秒", key, time);
			return 1L;
		});*/
	}

	/**
	 * 添加到带有 过期时间的 缓存
	 *
	 * @param key   redis主键
	 * @param value 值
	 * @param time  过期时间(单位秒)
	 */
	public void setExpire(final String key, final Object value, final long time) {
		//this.ex(key,value,time);
		this.opsForValue().set(key,value.toString(),time,TimeUnit.SECONDS);
		/*RedisConnection redisConnection = this.getConnectionFactory().getConnection();
		try{
			byte[] keys = STRING_SERIALIZER.serialize(key);
			byte[] values = STRING_SERIALIZER.serialize(value.toString());
			redisConnection.setEx(keys,time,values);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			redisConnection.close();
		}*/

		/*this.execute((RedisCallback<Long>) connection -> {
			RedisSerializer<String> serializer = getRedisSerializer();
			byte[] keys = serializer.serialize(key);
			byte[] values = OBJECT_SERIALIZER.serialize(value);
			connection.setEx(keys, time, values);
			return 1L;
		});*/
	}


	/**
	 * 添加到缓存
	 *
	 * @param key   the key
	 * @param value the value
	 */
	public void set(final String key, final Object value) {
		this.opsForValue().set(key,value.toString());
		/*this.execute((RedisCallback<Long>) connection -> {
			RedisSerializer<String> serializer = getRedisSerializer();
			byte[] keys = serializer.serialize(key);
			byte[] values = STRING_SERIALIZER.serialize(value.toString());
			connection.set(keys, values);
			log.debug("[redisTemplate redis]放入 缓存  url:{}", key);
			return 1L;
		});*/
	}

	/**
	 * 查询在这个时间段内即将过期的key
	 *
	 * @param key  the key
	 * @param time the time
	 * @return the list
	 */
	public List<String> willExpire(final String key, final long time) {
		final List<String> keysList = new ArrayList<>();
		this.execute((RedisCallback<List<String>>) connection -> {
			Set<String> keys = this.keys(key + "*");
			for (String key1 : keys) {
				Long ttl = connection.ttl(key1.getBytes(DEFAULT_CHARSET));
				if (0 <= ttl && ttl <= 2 * time) {
					keysList.add(key1);
				}
			}
			return keysList;
		});
		return keysList;
	}

	/**
	 * 查询在以keyPatten的所有 key
	 *
	 * @param keyPatten the key patten
	 * @return the set
	 */
	/*@Override
	public Set<String> setKeys(final String keyPatten) {
		return this.keys()
	}*/

	/**
	 * 根据key获取对象
	 *
	 * @param key the key
	 * @return the byte [ ]
	 */
	/*public byte[] get(final byte[] key) {
		this.opsForValue().get(key)
		byte[] result = this.execute((RedisCallback<byte[]>) connection -> connection.get(key));
		log.debug("[redisTemplate redis]取出 缓存  url:{} ", key);
		return result;
	}*/

	/**
	 * 根据key获取对象
	 *
	 * @param key the key
	 * @return the string
	 */
	public String get(final String key) {
		return this.opsForValue().get(key);
		/*Object resultStr = this.execute((RedisCallback<Object>) connection -> {
			RedisSerializer<String> serializer = getRedisSerializer();
			byte[] keys = serializer.serialize(key);
			byte[] values = connection.get(keys);
			return OBJECT_SERIALIZER.deserialize(values);
		});
		log.debug("[redisTemplate redis]取出 缓存  url:{} ", key);
		return resultStr;*/
	}
	
    /**
     * 根据key获取对象
     * @param key the key
     * @return the string
     */
    /*public String getString(final String key) {
        Object obj = get(key);
        log.debug("[redisTemplate redis]取出 缓存  url:{} ", key);
        if(null != obj) {
            return obj.toString();
        }
        return null;
    }*/

	/**
	 * 根据key获取对象
	 *
	 * @param keyPatten the key patten
	 * @return the keys values
	 */
	/*public Map<String, Object> getKeysValues(final String keyPatten) {
		log.debug("[redisTemplate redis]  getValues()  patten={} ", keyPatten);
		return this.execute((RedisCallback<Map<String, Object>>) connection -> {
			RedisSerializer<String> serializer = getRedisSerializer();
			Map<String, Object> maps = new HashMap<>(16);
			Set<String> keys = this.keys(keyPatten + "*");
			if (CollectionUtils.isNotEmpty(keys)) {
				for (String key : keys) {
					byte[] bKeys = serializer.serialize(key);
					byte[] bValues = connection.get(bKeys);
					Object value = STRING_SERIALIZER.deserialize(bValues);
					maps.put(key, value);
				}
			}
			return maps;
		});
	}*/

	/**
	 * Ops for hash hash operations.
	 *
	 * @return the hash operations
	 */
	/*@Override
	public HashOperations<String, String, Object> opsForHash() {
		return this.opsForHash();
	}*/

	/**
	 * 对HashMap操作
	 *
	 * @param key       the key
	 * @param hashKey   the hash key
	 * @param hashValue the hash value
	 */
	/*public void putHashValue(String key, String hashKey, Object hashValue) {
		log.debug("[redisTemplate redis]  putHashValue()  key={},hashKey={},hashValue={} ", key, hashKey, hashValue);
		opsForHash().put(key, hashKey, hashValue);
	}*/

	/**
	 * 获取单个field对应的值
	 *
	 * @param key     the key
	 * @param hashKey the hash key
	 * @return the hash values
	 */
	/*public Object getHashValues(String key, String hashKey) {
		log.debug("[redisTemplate redis]  getHashValues()  key={},hashKey={}", key, hashKey);
		return opsForHash().get(key, hashKey);
	}*/

	/**
	 * 根据key值删除
	 *
	 * @param key      the key
	 * @param hashKeys the hash keys
	 */
	/*public void delHashValues(String key, Object... hashKeys) {
		log.debug("[redisTemplate redis]  delHashValues()  key={}", key);
		opsForHash().delete(key, hashKeys);
	}*/

	/**
	 * key只匹配map
	 *
	 * @param key the key
	 * @return the hash value
	 */
	/*public Map<String, Object> getHashValue(String key) {
		log.debug("[redisTemplate redis]  getHashValue()  key={}", key);
		return opsForHash().entries(key);
	}*/

	/**
	 * 批量添加
	 *
	 * @param key the key
	 * @param map the map
	 */
	/*public void putHashValues(String key, Map<String, Object> map) {
		opsForHash().putAll(key, map);
	}*/

	/**
	 * 集合数量
	 *
	 * @return the long
	 */
	/*public long dbSize() {
		return this.execute(RedisServerCommands::dbSize);
	}*/

	/**
	 * 清空redis存储的数据
	 *
	 * @return the string
	 */
	/*public String flushDB() {
		return this.execute((RedisCallback<String>) connection -> {
			connection.flushDb();
			return "ok";
		});
	}*/

	/**
	 * 判断某个主键是否存在
	 *
	 * @param key the key
	 * @return the boolean
	 */
	public boolean exists(final String key) {
		return this.hasKey(key);
	}

	/**
	 * 删除key
	 *
	 * @param keys the keys
	 * @return the long
	 */
	public long del(final String... keys) {
		return this.delete(Arrays.asList(keys));
		/*return this.execute((RedisCallback<Long>) connection -> {
			long result = 0;
			for (String key : keys) {
				result = connection.del(key.getBytes(DEFAULT_CHARSET));
			}
			return result;
		});*/
	}

	/**
	 * 获取 RedisSerializer
	 *
	 * @return the redis serializer
	 */
	/*public RedisSerializer<String> getRedisSerializer() {
		return this.getStringSerializer();
	}*/

	/**
	 * 对某个主键对应的值加一,value值必须是全数字的字符串
	 *
	 * @param key the key
	 * @return the long
	 */
	public long incr(final String key) {
		return this.opsForValue().increment(key);
		/*return this.execute((RedisCallback<Long>) connection -> {
			RedisSerializer<String> redisSerializer = getRedisSerializer();
			return connection.incr(redisSerializer.serialize(key));
		});*/
	}

	/**
	 * 对某个主键对应的值加一,value值必须是全数字的字符串
	 *
	 * @param key the key
	 * @return the long
	 */
	public long incrBy(final String key,long value) {
		return this.opsForValue().increment(key,value);
		/*return this.execute((RedisCallback<Long>) connection -> {
			RedisSerializer<String> redisSerializer = getRedisSerializer();
			return connection.incr(redisSerializer.serialize(key));
		});*/
	}

	/**
	 * 对某个主键对应的值减一,value值必须是全数字的字符串
	 *
	 * @param key the key
	 * @return the long
	 */
	public long decr(final String key) {
		return this.opsForValue().decrement(key);
		/*return this.execute((RedisCallback<Long>) connection -> {
			RedisSerializer<String> redisSerializer = getRedisSerializer();
			return connection.decr(redisSerializer.serialize(key));
		});*/
	}

	/**
	 * redis List 引擎
	 *
	 * @return the list operations
	 */
	/*public ListOperations<String, String> opsForList() {
		return redisTemplate.opsForList();
	}*/

	/**
	 * redis List数据结构 : 将一个或多个值 value 插入到列表 key 的表头
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the long
	 */
	/*public Long leftPush(String key, Object value) {
		return opsForList().leftPush(key, value);
	}*/

	/**
	 * redis List数据结构 : 移除并返回列表 key 的头元素
	 *
	 * @param key the key
	 * @return the string
	 */
	/*public Object leftPop(String key) {
		return opsForList().leftPop(key);
	}*/

	/**
	 * redis List数据结构 :将一个或多个值 value 插入到列表 key 的表尾(最右边)。
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the long
	 */
	/*public Long in(String key, Object value) {
		return opsForList().rightPush(key, value);
	}*/

	/**
	 * redis List数据结构 : 移除并返回列表 key 的末尾元素
	 *
	 * @param key the key
	 * @return the string
	 */
	/*public Object rightPop(String key) {
		return opsForList().rightPop(key);
	}*/

	/**
	 * redis List数据结构 : 返回列表 key 的长度 ; 如果 key 不存在，则 key 被解释为一个空列表，返回 0 ; 如果 key
	 * 不是列表类型，返回一个错误。
	 *
	 * @param key the key
	 * @return the long
	 */
	/**public Long length(String key) {
		return opsForList().size(key);
	}*/

	/**
	 * redis List数据结构 : 根据参数 i 的值，移除列表中与参数 value 相等的元素
	 *
	 * @param key   the key
	 * @param i     the
	 * @param value the value
	 */
	/*public void remove(String key, long i, Object value) {
		opsForList().remove(key, i, value);
	}*/

	/**
	 * redis List数据结构 : 将列表 key 下标为 index 的元素的值设置为 value
	 *
	 * @param key   the key
	 * @param index the index
	 * @param value the value
	 */
	/*public void set(String key, long index, Object value) {
		opsForList().set(key, index, value);
	}*/

	/**
	 * redis List数据结构 : 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。
	 *
	 * @param key   the key
	 * @param start the start
	 * @param end   the end
	 * @return the list
	 */
	/*public List<Object> getList(String key, int start, int end) {
		return opsForList().range(key, start, end);
	}*/

	/**
	 * redis List数据结构 : 批量存储
	 *
	 * @param key  the key
	 * @param list the list
	 * @return the long
	 */
	/*public Long leftPushAll(String key, List<String> list) {
		return opsForList().leftPushAll(key, list);
	}*/

	/**
	 * redis List数据结构 : 将值 value 插入到列表 key 当中，位于值 index 之前或之后,默认之后。
	 *
	 * @param key   the key
	 * @param index the index
	 * @param value the value
	 */
	/*public void insert(String key, long index, Object value) {
		opsForList().set(key, index, value);
	}
*/
	/**
	 * 根据key获取对象
	 *
	 * @param keyPatten the key patten
	 * @return the keys values
	 */
	public Map<String, Object> getKeysValues(final String keyPatten) {
		log.debug("[redisTemplate redis]  getValues()  patten={} ", keyPatten);
		Map<String,Object> maps =new HashMap<>(16);
		Set<String> keys = this.keys(keyPatten + "*");
		if (CollectionUtils.isNotEmpty(keys)) {
			for (String key : keys) {
				String value = this.get(key);
				maps.put(key, value);
			}
		}
		return maps;
	}
}
