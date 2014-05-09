package de.sub.goobi.helper.tasks;

public abstract class CloneableLongRunningTask extends LongRunningTask {
	@Override
	public abstract CreateProcessesTask clone();
}
