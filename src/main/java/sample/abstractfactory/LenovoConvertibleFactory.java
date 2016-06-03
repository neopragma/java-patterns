package sample.abstractfactory;

import java.util.ArrayList;
import java.util.List;

public class LenovoConvertibleFactory implements PortableComputerFactory, Constants {

	@Override
	public List<PortableComputer> getPortableComputers() {
		List<PortableComputer> result = new ArrayList<PortableComputer>();
		result.add(new LenovoYoga_2_11());
		result.add(new LenovoYoga_2_13());
        return result;
	}

}
