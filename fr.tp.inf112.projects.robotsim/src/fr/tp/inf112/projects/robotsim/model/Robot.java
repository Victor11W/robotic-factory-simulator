package fr.tp.inf112.projects.robotsim.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {

	@JsonIgnore
	private static final long serialVersionUID = -1218857231970296747L;

	@JsonIgnore
	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	@JsonIgnore
	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

	@JsonInclude
	private final Battery battery;

	@JsonInclude
	private int speed;

	@JsonInclude
	private List<Component> targetComponents;

	@JsonIgnore
	private transient Iterator<Component> targetComponentsIterator;

	@JsonInclude
	private Component currTargetComponent;

	@JsonIgnore
	private transient Iterator<Position> currentPathPositionsIter;

	@JsonIgnore
	private transient boolean blocked;
	
	private Position nextPosition;

	@JsonIgnore
	private FactoryPathFinder pathFinder;

	public Robot() {
		this(null,null,null,null, "DefaultRobot");
	}
	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		nextPosition = null;
	}

	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}

	protected int getSpeed() {
		return speed;
	}

	protected void setSpeed(final int speed) {
		this.speed = speed;
	}
	
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}
	
	@Override
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
		}
		
		computePathToCurrentTargetComponent();

		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}
	
	private int moveToNextPathPosition() {
		final Motion motion = computeMotion();
		
		int displacement = motion == null ? 0 : motion.moveToTarget();

		if(displacement != 0) {
			notifyObservers();
		} else if(isLivelyLocked()) {
			final Position freeNeighbouringPosition = findFreeNeighbouringPosition();
			if (freeNeighbouringPosition != null) {
				nextPosition = freeNeighbouringPosition;
				displacement = moveToNextPathPosition();
				computePathToCurrentTargetComponent();
			}
		}
		
		return displacement;
	}
	
	private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}
	
	private Motion computeMotion() {
		if (!currentPathPositionsIter.hasNext()) {

			// There is no free path to the target
			blocked = true;
			
			return null;
		}
		
		final Position nextPosition = this.nextPosition == null ? currentPathPositionsIter.next() : this.nextPosition;
		final PositionedShape shape = new RectangularShape(nextPosition.getxCoordinate(),
				   										   nextPosition.getyCoordinate(),
				   										   2,
				   										   2);
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.nextPosition = nextPosition;
			
			return null;
		}

		this.nextPosition = null;
		
		return new Motion(getPosition(), nextPosition);
	}
	
	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
	
	@Override
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}

	public Position getNextPosition() {
		return nextPosition;
	}

	private boolean isLivelyLocked() {
		if (getNextPosition() == null) {
			return false;
		}
		final PositionedShape shape = new RectangularShape(getNextPosition().getxCoordinate(),
				getNextPosition().getyCoordinate(),
				2,
				2);
		final Component otherMobileComponent = getFactory().getMobileComponentAt(shape,this);
		//Will have to be modified when we add more MobileComponent types, but for now this works
		if(otherMobileComponent instanceof Robot otherRobot) {
			return getPosition().equals(otherRobot.getNextPosition());
		}
		return false;
	}

	//this is terrible, but it works
	private Position findFreeNeighbouringPosition() {
		Position up = new Position(getxCoordinate(),getyCoordinate() + 4);
		Position left = new Position(getxCoordinate() - 4,getyCoordinate());
		Position right = new Position(getxCoordinate() + 4,getyCoordinate());
		Position down = new Position(getxCoordinate(),getyCoordinate() - 4);
		Optional<Position> availablePosition = Arrays.stream((new Position[]{up, left, right, down})).filter(position -> {
			PositionedShape shape = new RectangularShape(position.getxCoordinate(), position.getyCoordinate(), 2, 2);
			return (getNextPosition().getxCoordinate() != position.getxCoordinate() || getNextPosition().getyCoordinate() != position.getyCoordinate())
					&& !getFactory().hasObstacleAt(shape);
		}).findFirst();
		return availablePosition.orElse(null);
	}
}
