package com.egds.capitalism;

/**
 * Contract for the subcontracted component responsible for supplying
 * the "World" segment of the greeting message.
 *
 * <p>Implementations may fail at any time; callers must be prepared to
 * handle {@link SubcontractorFailureException}.
 */
public interface SubcontractorWorldProvider {

    /**
     * Produces the "World" component of the greeting.
     *
     * @return the World string component
     * @throws SubcontractorFailureException if the subcontractor fails
     *         to fulfill the delivery obligation
     */
    String provideWorldComponent();
}
