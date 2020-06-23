package dao;

import java.util.List;

import bo.BoLink;
import bo.GroupOfBo;

public interface IDaoLink extends IDao {

	int getLinksSize();
	Integer getIdPosition(Class<?> daoClass);
	<T extends BoLink> List<T> getById(final long id) throws Exception;
	IDaoItem getDao(final int position);
	GroupOfBo getNewGroup() throws Exception;
	IDaoItem getOwner();
}
