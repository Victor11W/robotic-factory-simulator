package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

public class Position implements Serializable {

	@JsonIgnore
	private static final long serialVersionUID = 7274819087013715987L;

	@JsonInclude
	private int xCoordinate;

	@JsonInclude
	private int yCoordinate;

	public Position() {
		this(0,0);
	}
	public Position(final int xCoordinate, 
					final int yCoordinate) {
		super();

		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	public int getxCoordinate() {
		return xCoordinate;
	}

	public int getyCoordinate() {
		return yCoordinate;
	}

	public boolean setxCoordinate(final int xCoordinate) {
		if (this.xCoordinate == xCoordinate) {
			return false;
		}
		
		this.xCoordinate = xCoordinate;
		
		return true;
	}

	public boolean setyCoordinate(final int yCoordinate) {
		if (this.yCoordinate == yCoordinate) {
			return false;
		}
		
		this.yCoordinate = yCoordinate;
		
		return true;
	}
	
	@Override
	public boolean equals(final Object objectToCompare) {
		if (objectToCompare == null) {
			return false;
		}
		
		final Position position = (Position) objectToCompare;
		
		return getxCoordinate() == position.getxCoordinate() && getyCoordinate() == position.getyCoordinate();
	}
	
	@Override
	public String toString() {
		final StringBuilder strBuild = new StringBuilder("Position = (");
		strBuild.append(getxCoordinate());
		strBuild.append(", ");
		strBuild.append(getyCoordinate());
		strBuild.append(")");
		
		return strBuild.toString();
	}
}
