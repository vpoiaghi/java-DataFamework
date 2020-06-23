package cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bo.BoItem;

public class Cache {

	private final Map<Long, BoItem> idBeansCache = new HashMap<>();
	
	public final void put(BoItem bo) {

		if (bo != null) {
			idBeansCache.put(bo.getId(), bo);
		}
	}
	
	public final void putAll(List<? extends BoItem> boList) {

		if (boList != null) {
			boList.forEach(bo -> put(bo));
		}
	}

	@SuppressWarnings("unchecked")
	public final <T extends BoItem> T get(long id) {
		return (T) idBeansCache.get(id);
	}
	

}
