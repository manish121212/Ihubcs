package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.Authority;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.UserAuthority;
import com.logicoy.bpelmon.models.UserDTO;
import com.logicoy.bpelmon.models.UserDetails;

public interface UserRegistration {
	/**
	 * registering new user
	 * 
	 * @return string statement
	 */
	public int registerUser(UserDetails userDetails);

	public List<UserDetails> getAllUserDetails();

	public List<UserDetails> searchAllUserDetailsByUsername(UserDetails details);

	public int updateExistingUser(UserDetails details);

	public List<Authority> getAllUserRoles();

	public List<Authority> getSelectedUserRoles(String username);

	public int updateUserRoles(UserAuthority userAuthorities);

	public List<UserDetails> getUserDetailsByUserName(UserDetails details);
	
	public GenericResponseModel recoverPassword(String email);
        
    public GenericResponseModel changeUserPassword(String newPassword,UserDTO details);
	
	public GenericResponseModel updateUserProfile(UserDTO details);

}
