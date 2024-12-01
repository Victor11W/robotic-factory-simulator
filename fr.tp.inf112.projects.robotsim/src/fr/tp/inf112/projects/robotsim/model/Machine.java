package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Machine extends Component {
	@JsonIgnore
	private static final long serialVersionUID = -1568908860712776436L;

	public Machine() {
		this(new Area(),null,"DefaultMachine");
	}
	public Machine(final Area area,
				   final RectangularShape shape,
				   final String name) {
		super(area.getFactory(), shape, name);
		
		area.setMachine(this);
	}

	@Override
	public String toString() {
		return super.toString() + "]";
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
}
