package dao;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bo.BoLink;
import bo.GroupOfBo;
import cache.LinkCache;

public abstract class DaoLink<T extends BoLink> extends Dao<T> implements IDaoLink {

    private LinkCache cache = new LinkCache();
    
    private IDaoItem[] linkedTypedDaos;
    private String tableName;
	private String[] idColNames;

    protected final IDaoItem owner;
    private final Type[] daoTypes;
    private Integer ownerIdPosition;

    public DaoLink(IDaoItem owner, Type[] daoTypes) {
        super();
        this.owner = owner;
        this.daoTypes = daoTypes;
    }

    @Override
    public String getTableName() {

        if (tableName == null) {
            tableName = buildTableName();
        }

        return tableName;
    }
    
    @Override
    public IDaoItem getOwner() {
    	return owner;
    }

	@SuppressWarnings("unchecked")
	@Override
	protected T getNewBo(final ResultSet rs) throws SQLException {
		
		final String[] idColNames = getIdColNames();
		
		T bo = (T) new BoLink(idColNames.length);
		
		buildBo(rs, bo);
		return bo;
	}

    public GroupOfBo getNewGroup() throws Exception {
    	if (linkedTypedDaos.length > 2) {
    		throw new Exception("getNewGroup() must be overrides by current class " + this.getClass().getName());	
    	} else {
    		throw new Exception("getNewGroup() can't be called for current class " + this.getClass().getName());
    	}
    }
	
    @Override
    protected void buildBo(final ResultSet rs, final T bo) throws SQLException {

        final String[] idColNames = getIdColNames();

        for (int i = 0; i <= idColNames.length - 1; i++) {
            bo.setId(i, rs.getLong(i + 1));
        }
    }

	@Override
	protected void boLoaded(final T bo) {
	}

    private IDaoItem[] getLinkedTypedDaos() {

        if (linkedTypedDaos == null) {
            linkedTypedDaos = buildLinkedDaosArray(daoTypes);
        }

        return linkedTypedDaos;
    }
    
    private String[] getIdColNames() {

        if (idColNames == null) {
            idColNames = buildColNames();
        }

        return idColNames;
    }

    private String[] buildColNames() {

        final IDaoItem[] linkedTypedDaos = getLinkedTypedDaos();
        final String[] idColNames = new String[linkedTypedDaos.length];
        String tblName;

        for (int i = 0; i < linkedTypedDaos.length; i++) {

            tblName = linkedTypedDaos[i].getTableName();
            tblName = tblName.substring(0, 1).toUpperCase() + tblName.substring(1);

            idColNames[i] = "id" + tblName;
        }

        return idColNames;
    }

    private String buildTableName() {

        final IDaoItem[] linkedTypedDaos = getLinkedTypedDaos();
        final StringBuilder tblName = new StringBuilder(linkedTypedDaos[0].getTableName());

        for (int i = 1; i < linkedTypedDaos.length; i++) {
            tblName.append("_").append(linkedTypedDaos[i].getTableName());
        }

        return tblName.toString();
    }

    @SuppressWarnings("unchecked")
	private IDaoItem[] buildLinkedDaosArray(Type[] linkedDaoTypes) {

        final IDaoItem[] DaoItemsArray = new DaoItem[linkedDaoTypes.length];

        for (int i = 0; i < linkedDaoTypes.length; i++) {
            DaoItemsArray[i] = FactoryOfDao.get((Class<? extends IDaoItem>) linkedDaoTypes[i]);
        }

        return DaoItemsArray;
    }

	@SuppressWarnings("unchecked")
	public List<T> getById(final long id) throws Exception {

        int idPosition = getIdPosition(owner.getClass());
        List<T> boList = getFromCache(id);

        if (boList.isEmpty()) {

            final String[] idColNames = getIdColNames();

            String rqt
                    = " SELECT * "
                    + " FROM " + getTableName()
                    + " WHERE " + idColNames[idPosition] + " = " + id;

            boList = getBoFromSource(rqt);
        }

        return boList;
    }

    @Override
    protected final void addToCache(BoLink bo) {

        int idPosition = getOwnerIdPosition();
        long id = bo.getId(idPosition);

        Map<Integer, BoLink> map = cache.get(id);

        if (map == null) {
            map = new HashMap<>();
            cache.put(id, map);
        }

        map.put(bo.getKey(), bo);
    }

    @SuppressWarnings("unchecked")
    protected List<T> getFromCache(long id) {

        List<T> list = new ArrayList<>();

        Map<Integer, BoLink> map = cache.get(id);

        if (map != null) {
        	map.values().forEach(bo -> list.add((T) bo));
        }

        return list;
    }

    public Integer getIdPosition(Class<?> daoClass) {

        final IDaoItem[] linkedTypedDaos = getLinkedTypedDaos();

        for (int i = 0; i < linkedTypedDaos.length; i++) {
            if (linkedTypedDaos[i].getClass() == daoClass) {
                return i;
            }
        }

        return null;
    }
    
    private int getOwnerIdPosition() {

        if (ownerIdPosition == null) {
            ownerIdPosition = getIdPosition(owner.getClass());
        }

        return ownerIdPosition;
    }

    /**
     * @return int le nombre de BO entrant en jeu dans la liaison (liaison bipartie : 2, liaison tripartie : 3, etc..)
     */
    public int getLinksSize() {
        return getLinkedTypedDaos().length;
    }

    public IDaoItem getDao(final int position) {
        final IDaoItem[] linkedTypedDaos = getLinkedTypedDaos();
        return linkedTypedDaos[position];
    }


	@Override
	public long save(T bo) {
		return 0;
	}

}
