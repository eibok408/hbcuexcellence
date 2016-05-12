/**
 * @author Antonio McMichael
 *
 */

package utils;

import play.data.validation.Email;
import play.data.validation.Required;

/**
 * A view model for passing in a contact request.
 */
public class Contact
{
	/**
	 * Name of the person.
	 */
	@Required
	public String name;
	
	/**
	 * Reply email address.
	 */
	@Email
	@Required
	public String email;
	
	/**
	 * The message to deliver.
	 */
	@Required
	public String message;
}
