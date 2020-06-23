package bo;

public abstract class Bo {

    /**
     * Indique si le bo existe en base ou si il s'agit d'un nouveau
     * bo non encore enregistré.
     * Cet attribut ne correspond pas à un champ en base.
     */
    private boolean inbase;

    public boolean isInbase() {
        return inbase;
    }

    public void setInbase(boolean inbase) {
        this.inbase = inbase;
    }

}
