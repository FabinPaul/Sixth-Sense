package org.mainproject.sixthsense;

public interface Communicator {
	
	//To communicate override this function in SixthSenseAvtivity
	public void respond(int id);
	
	public void saveButtonVisibility();

}
