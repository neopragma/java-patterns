package sample.abstractfactory;

public interface Convertible extends PortableComputer {
	
	default ComputerType getComputerType() {
		return ComputerType.CONVERTIBLE;
	}

}
