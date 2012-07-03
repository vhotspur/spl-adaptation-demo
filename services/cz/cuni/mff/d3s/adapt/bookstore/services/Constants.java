package cz.cuni.mff.d3s.adapt.bookstore.services;

public class Constants {
	/* SLA the server acts upon. */
	public static final int SLA_REQUEST_LENGTH_SERVER_SIDE_MILLIS = 8;
	
	/* Request processing takes at least this time. */
	public static final int REQUEST_LENGTH_FIXED_MILLIS = 2;
	/* And up to this millis might be added. */
	public static final int REQUEST_LENGTH_VARIABLE_MILLIS = 2;
	
	/* Client reports violation when requests takes more than this. */
	public static final int SLA_REQUEST_LENGTH_CLIENT_SIDE_MILLIS = 10;
	
	/* Client waits at least this time between consecutive actions. */
	public static final int CLIENT_PAUSE_BETWEEN_ACTIONS_FIXED_MILLIS = 2;
	/* And up to this millis might be added. */
	public static final int CLIENT_PAUSE_BETWEEN_ACTIONS_VARIABLE_MILLIS = 2;
	
	/* How much to raise actual (computed mean) request processing time
	 * prior comparison with the SLA.
	 * The formula is:  mean * SAFE_MARGIN > SLA ==> Action("SLA violated")
	 */
	public static final double SLA_SAFE_MARGIN = 1.2;
}
