package gl4.insat.tn.bigdatamobile.entities;

public class RSSItemAdress {
	public String formatted_address;
    public String adr(){
    	String route="";
    	int i=0;
    	char c=formatted_address.charAt(i);
    	
    	while(i<formatted_address.length()&&(c!=',')&&c!='،'){
    		route=route + formatted_address.charAt(i);
    		i++;
    		c=formatted_address.charAt(i);
    	}
    	route=route+", ";

        return route;
    }
}
