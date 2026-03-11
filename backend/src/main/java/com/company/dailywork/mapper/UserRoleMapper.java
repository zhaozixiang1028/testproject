package com.company.dailywork.mapper;

import com.company.dailywork.mapper.model.UserRoleRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserRoleMapper {

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("SELECT id FROM sys_role WHERE code = #{code} LIMIT 1")
    Long findRoleIdByCode(@Param("code") String code);

    @Select("SELECT r.code FROM sys_user_role ur JOIN sys_role r ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
    List<String> listRoleCodesByUserId(@Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT ur.user_id AS userId, r.code AS roleCode",
            "FROM sys_user_role ur",
            "JOIN sys_role r ON ur.role_id = r.id",
            "WHERE ur.user_id IN",
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<UserRoleRecord> listRoleCodesByUserIds(@Param("userIds") List<Long> userIds);
}
