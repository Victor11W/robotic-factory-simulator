package fr.tp.inf112.projects.robotsim.model.shapes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.tp.inf112.projects.canvas.model.OvalShape;

public class CircularShape extends PositionedShape implements OvalShape {
	@JsonIgnore
	private static final long serialVersionUID = -1912941556210518344L;

	@JsonInclude
	public final int radius;

	public CircularShape() {
		this(0,0,-1);
	}
	public CircularShape( 	final int xCoordinate,
							final int yCoordinate,
							final int radius ) {
		super( xCoordinate, yCoordinate );
		
		this.radius = radius;
	}

	@JsonIgnore
	@Override
	public int getWidth() {
		return 2 * radius;
	}

	@JsonIgnore
	@Override
	public int getHeight() {
		return getWidth();
	}

	@Override
	public String toString() {
		return super.toString() + " [radius=" + radius + "]";
	}
}
