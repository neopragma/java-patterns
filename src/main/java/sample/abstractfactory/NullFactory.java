package sample.abstractfactory;

import java.util.ArrayList;
import java.util.List;

public class NullFactory implements PortableComputerFactory {

	@Override
	public List<PortableComputer> getPortableComputers() {
		return new ArrayList<PortableComputer>();
	}

}
