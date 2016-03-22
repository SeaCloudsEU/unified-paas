package eu.seaclouds.paas.openshift2;

import eu.seaclouds.paas.PaasSession;


/**
 * 
 *
 * @author ATOS
 * @date 21/3/2016-13:49:55
 */
public class DeployParameters implements PaasSession.DeployParameters {

	
    private String path;
    private String cartridge;

    
    public DeployParameters(String path, String cartridge) {
        this.path = path;
        this.cartridge = cartridge;
    }
    
    
    @Override
    public String getPath() {
        return path;
    }


    @Override
	public String getCartridge()
	{
		return cartridge;
	}


	@Override
	public String getBuildpackUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
