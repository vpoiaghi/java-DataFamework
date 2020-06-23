package dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import data_source.IDataSource;

public final class FactoryOfDao {

	private static final Map<Class<? extends IDaoItem>, IDaoItem> daoItemMap = new HashMap<>();
	private static final Map<Integer, IDaoLink> daoLinkMap = new HashMap<>();
	
	public static <T extends IDaoItem> void declareDao(final T dao, final IDataSource dataSource) {

		try {
			
			Class<? extends IDaoItem> daoType = dao.getClass();
			
			dao.setDataSource(dataSource);
			daoItemMap.put(daoType, dao);
			
		} catch (IllegalArgumentException |  SecurityException e) {
			e.printStackTrace();
		}

	}
	
	public static <T extends IDaoLink> void declareDaoLink(final T daoLink, final IDataSource dataSource) {
		
		//IDaoItem owner;
		int key;
		
		//for(int position = 0; position < daoLink.getLinksSize(); position++) {
		
	        try {
	        	
	        	//owner = daoLink.getDao(position);
	        	//key = getDaoLinkKey(owner, daoLink.getClass());
	        	key = getDaoLinkKey(daoLink.getOwner(), daoLink.getClass());
	        	
				daoLink.setDataSource(dataSource);
				
				daoLinkMap.put(key, daoLink);
	            
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
		//}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends IDaoItem> T get(Class<T> type) {
		return (T) daoItemMap.get(type);
	}


    // Retourne une instance unique d'un LinkDao
    // Usage : DaoBookAuthor d = FactoryDao.get(daoBook, DaoBookAuthor.class);
    @SuppressWarnings("unchecked")
	public static <T extends IDaoLink> T get(final IDaoItem owner, final Class<T> type) {

        T dao = null;

        int key = getDaoLinkKey(owner, type);

        try {
            dao = (T) daoLinkMap.get(key);
        } catch(ClassCastException e) {
        }

        return dao;

    }
    
    private static <T extends IDaoLink> int getDaoLinkKey(final IDaoItem owner, final Class<T> type) {
    	return Objects.hash(owner.getClass().getName(), type.getName());	
    }
}
