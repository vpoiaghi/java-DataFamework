package dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bo.Bo;
import bo.BoItem;
import bo.BoLink;
import bo.GroupOfBo;
import cache.Cache;
import connection.SourceConnection;
import lombok.Getter;
import lombok.Setter;

public abstract class DaoItem<T extends BoItem> extends Dao<T> implements IDaoItem {

	private static final String COL_ID = "Id";
	private static final String COL_TSP = "TSP";

	private final Cache cache = new Cache();
	private Class<T> boClass;
	
    // liste des dao liés
    private DaoLinks daoLinks;

    
    protected abstract void initLinks(final DaoLinks daoLinks);
    
	@SuppressWarnings("unchecked")
	@Override
	protected T getNewBo(final ResultSet rs) throws Exception {

		T bo = null;
		
		Class<T> pType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		
		try {
			bo = (T) pType.getDeclaredConstructors()[0].newInstance((Object[]) null);
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new Exception(e);
		}
		
		if (bo != null) {
			bo.setId(rs.getLong(COL_ID));
			bo.setTsp(dateToLocalDateTime(rs.getString(COL_TSP)));
		}

		try {
			buildBo(rs, bo);
		} catch (SQLException e) {
			throw new Exception(e);
		}

    	return bo;
	}
	
	@Override
	protected void boLoaded(final T bo) throws Exception {
		applyLinks(bo);		
	}
	
	/**
	 * 
	 * @param sqlbook
	 */
	protected long getNextId(final String tableName) throws Exception {
	
		final String sql = "SELECT MAX(id) AS maxId FROM " + tableName;
		long result = -1;
		
		SourceConnection conn = null;
		
		try {

			conn = openConnection();
			ResultSet rs = conn.execute(sql);
            
            long newId = 0;
            while (rs.next()) {
            	newId = rs.getInt("maxId");
            }
        	result = newId + 1;

		} catch (Exception e) {
        	throw e;
        } finally {
        	closeConnection(conn);
        }

		return result;		
		
	}

	protected final List<T> getBoList(final String rqt) throws Exception {
		
		List<T> boList = null;

		final List<Long> ids = new ArrayList<>();
		SourceConnection conn = openConnection();

        ResultSet rs = conn.execute(rqt);

        while (rs.next()) {
        	ids.add(rs.getLong(1));
        }
            
		if (ids.size() == 0) {
			boList = new ArrayList<>();	
		} else {
			boList = getBoList(ids);
		}
		
    	closeConnection(conn);
    	
		return boList;

	}
	
	private final List<T> getBoList(final List<Long> ids) throws Exception {
		
		List<T> boList = new ArrayList<>();
		List<Long> idsOfBeansNotInCache = new ArrayList<>();
		
		// liste ids des bo non connus en cache
		for (Long id : ids) {
			if (cache.get(id) == null) {
				idsOfBeansNotInCache.add(id);
			}
		}
		
		// si des bo n'ont pas �t� trouv�s en cache, recherche en base et chargement dans le cache
		if (idsOfBeansNotInCache.size() > 0) {
			loadBoListFromBaseInCache(idsOfBeansNotInCache);
		}
		
		// recherche des bo en cache, le cache possedant tous les bo recherchés
		for (Long id : ids) {
			boList.add(cache.get(id));
		}
		
		return boList;
	}
	
	@Override
	protected void addToCache(final T bo) {
		cache.put(bo);
	}
	
	protected T getFromCache(final long id) {
		return cache.get(id);
	}
	
	
	/**
	 * Load bo list from base to cache. bo are searched by id.
	 * @param ids list of searched id list.
	 */
	private void loadBoListFromBaseInCache(List<Long> ids) throws Exception {
		
		int maxListSize = 100;
		int i = 0;
		int j;
		int idsListSize = ids.size(); 
		int lastIndex;
		String rqt = "";
		
		while (i < idsListSize) {
			
			lastIndex = Math.min(idsListSize, i + maxListSize) - 1;
			
			rqt = ids.get(0).toString();
			for (j = i + 1; j <= lastIndex; j++) {
				rqt += "," + ids.get(j);
			}
			
			rqt = "SELECT * FROM " + getTableName() + " WHERE Id IN (" + rqt + ")";
			getBoFromSource(rqt);
			
			i += maxListSize;
		}
		
	}
	
	/**
	 * @return list of all bo of specific bo type
	 */
	public List<T> getAll() throws Exception {
		return getBoList("SELECT id FROM " + getTableName());
	}

	/**
	 * Return list of bo have changes since specific datetime (timestamp) 
	 * @param tsp date to filter
	 * @return list of bo
	 */
	public List<T> getChanged(LocalDateTime tsp) throws Exception {
		return getBoList("SELECT id FROM " + getTableName() + " WHERE tsp >= " + getSqlDate(tsp)); 
	}

    @Override
    public long save(final T bo) {

        final long id = -1;
    	
    	/*
        final ContentValues values = new ContentValues();

        values.put(COL_TSP, new SqlDate(DateUtils.getNow()).getValue());
        setContentValues(values, bo);

        if (bo.isInbase()) {
            id = bo.getId();
            doUpdate(values, id);
        } else {
            final String tableName = getTableName();
            id = getNewId(tableName);
            values.put(COL_ID, id);
            doInsert(values);
        }

        bo.setInbase(true);
        addToCache(bo);
    	*/

        return id;
    }

    private DaoLinks getDaoLinks() {

        if (daoLinks == null) {
        	daoLinks = new DaoLinks();
            initLinks(daoLinks);
        }

        return daoLinks;
    }

    private void applyLinks(final T bo) throws Exception {

        int idPositionForMe;
        int idPositionForOther;

        IDaoItem daoOther;
        Class<? extends BoItem> linkedBoClass;

        // Récupération des types de liaison pour de BO
        Map<Class<? extends Bo>, List<? extends Bo>> links = bo.getLinks();

        // Parcours des types de liaison
        for (IDaoLink daoLinkType : getDaoLinks().getDaoLinkList()) {

            // Compléxité de la liaison (biparties, triparties, etc...)
            int linksPartsSize = daoLinkType.getLinksSize();

            // Position du BO courant dans la liaison (= numéro colonne dans la table de liaison)
            idPositionForMe = daoLinkType.getIdPosition(this.getClass());
            
            // Liste des BO de liaison pour le type de liaison en cours
            List<BoLink> boLinks = daoLinkType.getById(bo.getId());

            List<KeyList> tmpKeyLists = new ArrayList<>();
            
            // Parcours des ids (liens) entrant en jeu dans la liaison
            for (idPositionForOther = 0; idPositionForOther < linksPartsSize; idPositionForOther++) {

                // Récupérarion des BO liés pour le type de liaison en cours
                if (idPositionForOther != idPositionForMe) {
                    // Pas la peine de me récupérer moi-même

                    // DAO lié
                    daoOther = daoLinkType.getDao(idPositionForOther);

                    // Type de BO lié
                    linkedBoClass = daoOther.getBoClass();

                    // Lecture des id des BO liés et récupéréation du BO liés pour ajout à la liste
                    List<Long> idList = new ArrayList<>();
                    for (BoLink boLink : boLinks) {
                        long id = boLink.getId(idPositionForOther);
                        idList.add(id);
                    }

                    // Alimentation de la liste des BO liés pour ce type de lien
                    //links.put(linkedBoClass, (List<? extends BoItem>) daoOther.getById(idList));
                    tmpKeyLists.add(new KeyList(linkedBoClass, (List<? extends BoItem>) daoOther.getById(idList)));
                }
            }

            if (linksPartsSize == 2) {
            	links.put(tmpKeyLists.get(0).getKey(), tmpKeyLists.get(0).getList());
            } else {
            	
            	// 1 - Récupérer le bon GroupBo (classe héritant de Bo et regroupant des BoItem ex : Author + Role)
            	Class<? extends GroupOfBo> groupType = daoLinkType.getNewGroup().getClass();
            	GroupOfBo group;
            	
            	// 2 - Alimenter le groupBo avec les BO de tmpKeyLists (parcours des n listes en même temps et récupération des 
            	//     bo sur ces listes à la position p pour alimenter le GroupBo
            	List<GroupOfBo> groups = new ArrayList<>();
            	for (int i = 0; i < tmpKeyLists.get(0).getList().size(); i ++) {

            		group = daoLinkType.getNewGroup();
	            	
            		for (int j = 0; j < linksPartsSize - 1; j++) {
	            		group.setBoItem(tmpKeyLists.get(j).getList().get(i));
	            	}
            	
	            	// 3 - Ajouter le groupBo une liste de GroupBo
	            	groups.add(group);
            	}
            	
            	// 4 - Une fois la liste des GroupBo complète, ajouter cette liste à links comme fait quand linksPartsSize == 2
            	links.put(groupType, groups);
            }
            
        }
    }

    @SuppressWarnings("unchecked")
	public Class<T> getBoClass() {

        if (boClass == null) {
            boClass = (Class<T>) ((ParameterizedType) Objects.requireNonNull(getClass().getGenericSuperclass())).getActualTypeArguments()[0];
        }

        return boClass;
    }

	/**
	 * @return list of all bo of specific bo type
	 */
	@SuppressWarnings("unchecked")
	public T getById(final long id) throws Exception {
		List<Long> ids = new ArrayList<>();
		ids.add(id);
		List<T> boList = getBoList(ids);
		return boList.size() > 0 ? boList.get(0) : null;
	}

    public List<T> getById(final List<Long> idList) throws Exception {

        final List<T> resultList = new ArrayList<>();

        if (idList != null && idList.size() > 0) {

            final List<Long> unknownBoIdList = new ArrayList<>();
            T bo;

            for (Long id : idList) {
                bo = getFromCache(id);

                if (bo == null) {
                    unknownBoIdList.add(id);
                }
            }

            if (unknownBoIdList.size() > 0) {

                int index;
                int startIndex = 0;
                int endIndex = 399;
                int listLastIndex = unknownBoIdList.size() - 1;

                StringBuffer stringIds;

                boolean ended = false;

                while (!ended) {

                    stringIds = new StringBuffer();

                    if (listLastIndex < endIndex) {
                        endIndex = listLastIndex;
                        ended = true;
                    }

                    for (index = startIndex; index <= endIndex; index++) {
                        stringIds.append(",").append(unknownBoIdList.get(index));
                    }

                    resultList.addAll(getBoFromSource("SELECT * FROM " + getTableName() + " WHERE " + COL_ID + " IN (" + stringIds.toString().substring(1) + ")"));

                    startIndex += 400;
                    endIndex += 400;
                }

                for (T boResult : resultList) {
                    addToCache(boResult);
                }

                resultList.clear();
            }

            for (Long id : idList) {
                resultList.add(getFromCache(id));
            }

        }

        return resultList;
    }

    @Getter @Setter
    private class KeyList {
    	private final Class<? extends BoItem> key;
    	private final List<? extends BoItem> list;
		public KeyList(final Class<? extends BoItem> key, final List<? extends BoItem> list) {
			this.key = key;
			this.list = list;
		}
    }
}
