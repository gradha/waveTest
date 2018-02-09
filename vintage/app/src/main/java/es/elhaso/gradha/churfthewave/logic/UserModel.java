package es.elhaso.gradha.churfthewave.logic;

import es.elhaso.gradha.churfthewave.network.JSONUser;

/**
 * Business logic model.
 */
public class UserModel
{
    public final long id;
    public final String firstName;
    public final String lastName;
    public final String gender;

    public UserModel(long id,
        String firstName,
        String lastName,
        String gender)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
    }

    public UserModel(JSONUser jsonUser)
    {
        id = jsonUser.id;
        firstName = jsonUser.firstName;
        lastName = jsonUser.lastName;
        gender = jsonUser.gender;
    }
}
