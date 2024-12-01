package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

public abstract class Component implements Figure, Serializable, Runnable {
	@JsonIgnore
	private static final Logger logger = Logger.getLogger(Component.class.getName());

	@JsonIgnore
	private static final long serialVersionUID = -5960950869184030220L;

	@JsonInclude
	private String id;

	@JsonBackReference
	private final Factory factory;

	@JsonInclude
	private final PositionedShape positionedShape;

	@JsonInclude
	private final String name;

	protected Component() {
		this(null,null,"DefaultComponent");
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
		return getPositionedShape().getPosition();
	}

	protected Factory getFactory() {
		return factory;
	}

	@JsonIgnore
	@Override
	public int getxCoordinate() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getxCoordinate();
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
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.severe(e.getMessage());
			}
		}
	}
}
