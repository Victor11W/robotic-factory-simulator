package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

public abstract class Component implements Figure, Serializable, Runnable {
	
	private static final long serialVersionUID = -5960950869184030220L;

	@JsonInclude
	public String id;

	@JsonBackReference
	private final Factory factory;

	@JsonInclude
	public final PositionedShape positionedShape;

	@JsonInclude
	public final String name;

	protected Component() {
		this(null, null, "Component");
	}

	protected Component(final Factory factory,
						final PositionedShape shape,
						final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;

		if (factory != null) {
			factory.addComponent(this);
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PositionedShape getPositionedShape() {
		return positionedShape;
	}

	@JsonIgnore
	public Position getPosition() {
		PositionedShape shape = getPositionedShape();
		if(shape == null) {
			return new Position(-1, -1);
		}
		return shape.getPosition();
	}

	public Factory getFactory() {
		return factory;
	}

	@JsonIgnore
	@Override
	public int getxCoordinate() {
		PositionedShape shape = getPositionedShape();
		if(shape == null) {
			return -1;
		}
		return shape.getxCoordinate();
	}

	protected boolean setxCoordinate(int xCoordinate) {
		if ( getPositionedShape().setxCoordinate( xCoordinate ) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	@JsonIgnore
	@Override
	public int getyCoordinate() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getyCoordinate();
	}

	protected boolean setyCoordinate(final int yCoordinate) {
		if (getPositionedShape().setyCoordinate(yCoordinate) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	protected void notifyObservers() {
		getFactory().notifyObservers();
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
				+ ", shape=" + getPositionedShape();
	}

	@JsonIgnore
	public int getWidth() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getWidth();
	}

	@JsonIgnore
	public int getHeight() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getHeight();
	}
	
	public boolean behave() {
		return false;
	}

	@JsonIgnore
	public boolean isMobile() {
		return false;
	}
	
	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}
	
	public boolean overlays(final PositionedShape shape) {
		return getPositionedShape().overlays(shape);
	}
	
	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}

	@JsonIgnore
	@Override
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}

	@JsonIgnore
	@Override
	public Shape getShape() {
		return getPositionedShape();
	}

	@JsonIgnore
	public boolean isSimulationStarted() {
		return getFactory().isSimulationStarted();
	}

	@Override
	public void run(){
		while(isSimulationStarted()) {
			behave();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
