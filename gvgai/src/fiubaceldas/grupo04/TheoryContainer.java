package fiubaceldas.grupo04;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TheoryContainer implements Iterable<Theory> {

	private List<Theory> list = new LinkedList<Theory>();

	public TheoryContainer() {
	}

	/* Agrega, y si habia teorias iguales o mas especificas, las borra */
	public void addOrReplace(Theory t) {
		Iterator<Theory> iter = list.iterator();
		while (iter.hasNext()) {
			Theory l = iter.next();
			if (l.equals(t)) {
				iter.remove();
			}
		}
		list.add(t);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public int size() {
		return list.size();
	}

	public List<Theory> returnEqualTheories(Theory source) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : list) {
			if (source.equals(t))
				ret.add(t);
		}
		return ret;
	}

	public List<Theory> returnSimilarTheories(Theory source) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : list) {
			if (source.similar(t))
				ret.add(t);
		}
		return ret;
	}

	public List<Theory> returnIncompatibleTheories(Theory source) {
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : list) {
			if (source.incompatible(t)) {
				ret.add(t);
			}
		}
		return ret;
	}

	public List<Theory> returnApplicableTheories(State currentState) {
		Predicates stateDesc = new Predicates(currentState);
		List<Theory> ret = new ArrayList<Theory>();
		for (Theory t : list) {
			if (t.isApplicableToState(stateDesc) && t.getSuccessRate() >= 0.80) {
				ret.add(t);
			}
		}
		return ret;
	}

	@Override
	public Iterator<Theory> iterator() {
		return list.iterator();
	}
}
