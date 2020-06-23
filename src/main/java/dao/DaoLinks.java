package dao;

import java.util.ArrayList;
import java.util.List;

public class DaoLinks {
	
	private List<IDaoLink> daoLinkList;
	
	public DaoLinks() {
		daoLinkList = new ArrayList<>();
	}
	
	public void add(final IDaoLink dao) {
		daoLinkList.add(dao);
	}

	public List<IDaoLink> getDaoLinkList() {
		return daoLinkList;	
	}

}
