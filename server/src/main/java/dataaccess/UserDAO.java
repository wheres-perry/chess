// package dataaccess;

// import model.UserData;

// //Auto generated comments

// /**
//  * Interface for User data access operations
//  */
// public interface UserDAO {
//     /**
//      * Creates a new user in the database
//      * 
//      * @param user the user to create
//      * @throws DataAccessException if there is an error creating the user
//      */
//     void createUser(UserData user) throws DataAccessException;

//     /**
//      * Gets a user from the database by username
//      * 
//      * @param username the username to look up
//      * @return the user data
//      * @throws DataAccessException if there is an error retrieving the user
//      */
//     UserData getUser(String username) throws DataAccessException;

//     /**
//      * Clears all users from the database
//      * 
//      * @throws DataAccessException if there is an error clearing users
//      */
//     void clear() throws DataAccessException;
// }