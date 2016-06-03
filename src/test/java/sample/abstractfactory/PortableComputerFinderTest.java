package sample.abstractfactory;

//import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PortableComputerFinderTest {
	
	private PortableComputerFinder finder;
	
	@Before
	public void beforeEachTestCase() {
		finder = new PortableComputerFinder();
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void itFindsLenovoConvertibles() {
		List<Class> expected = new ArrayList<Class>(Arrays.asList(LenovoYoga_2_11.class, LenovoYoga_2_13.class));
		List<PortableComputer> list = finder.find(ComputerType.CONVERTIBLE, Manufacturer.LENOVO);
        List<Class> actual = new ArrayList<Class>();
        for (PortableComputer entry : list) {
        	actual.add(entry.getClass());
        }
        assertThat(actual, is(expected));        
	}

}
