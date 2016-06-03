package sample.abstractfactory;

import java.util.List;

public class PortableComputerFinder {
	
	private PortableComputerFactory factory;
	
	public List<PortableComputer> find(ComputerType computerType, Manufacturer manufacturer) {
        switch (manufacturer) {
        case LENOVO : 
            switch (computerType) {
            case CONVERTIBLE : 
            	factory = new LenovoConvertibleFactory();
            	break;
            default : 
            	factory = new NullFactory();
            	break;
            }
            break;
        default : factory = new NullFactory();    
        }
        return factory.getPortableComputers();
	}

}
