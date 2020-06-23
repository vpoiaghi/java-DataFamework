package bo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BoItem extends Bo implements Comparable<BoItem> {

	private Map<Class<? extends Bo>, List<? extends Bo>> links = new HashMap<>();
	
	private long id;
	private LocalDateTime tsp;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the tsp
	 */
	public LocalDateTime getTsp() {
		return tsp;
	}

	/**
	 * @param tsp the tsp to set
	 */
	public void setTsp(LocalDateTime tsp) {
		this.tsp = tsp;
	}

    public Map<Class<? extends Bo>, List<? extends Bo>> getLinks() {
        return links;
    }

    @SuppressWarnings("unchecked")
	protected <T extends Bo> List<T> getLinkedBoList(Class<T> linkedBoClass) {

        List<T> list = (List<T>) links.get(linkedBoClass);

        if (list == null) {
            list = new ArrayList<>();
            links.put((Class<? extends Bo>) linkedBoClass, (List<BoItem>) list);
        }

        return list;
    }

    @SuppressWarnings("unchecked")
	protected <T extends BoItem> T getLinkedBo(Class<T> linkedBoClass) {

        List<T> list = (List<T>) links.get(linkedBoClass);

        if (list == null) {
            list = new ArrayList<>();
            links.put((Class<BoItem>) linkedBoClass, (List<BoItem>) list);
        }
        
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public int compareTo(BoItem o) {

        long myId = getId();
        long otherId = o.getId();

        if (myId == otherId) {
            return 0;
        } else {
            return myId > otherId ? 1 : -1;
        }
    }

    @Override
    public boolean equals(Object o){

        if (o instanceof BoItem) {
        	Long thisId = new Long(id);
        	Long otherId = new Long(((BoItem) o).getId());
            return thisId.equals(otherId);
        } else {
            return false;
        }
    }

}
