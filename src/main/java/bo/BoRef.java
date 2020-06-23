package bo;

import dao.FactoryOfDao;
import dao.IDaoItem;

public class BoRef<B extends BoItem> {

	private IDaoItem dao;
	private B bo;

	@SuppressWarnings("unchecked")
	public <D extends IDaoItem> BoRef(Class<D> daoType, final Long id) throws Exception {
		
		this.dao = FactoryOfDao.get(daoType);
		this.bo = (B) this.dao.getById(id);
	}
	
	public IDaoItem getDao() {
		return this.dao;
	}

	public B getBo() {
		return this.bo;
	}
}
