package bo;

import java.util.Objects;

public class BoLink extends Bo {

    // technical ids.
    private final Long[] ids;

    public BoLink(int size) {
        super();
        ids = new Long[size];
    }

    public Long getId(int index) {
        return ids[index];
    }

    public Long[] getIds() {
        return ids;
    }

    public void setId(int index, long id) {
        ids[index] = id;
    }

    public int getKey() {
        return Objects.hash((Object[]) getIds());
    }

}
