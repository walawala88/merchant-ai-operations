package com.merchant.ops.service;

import com.merchant.ops.dto.EmployeeDTO;
import com.merchant.ops.dto.EmployeeLoginDTO;
import com.merchant.ops.dto.EmployeePageQueryDTO;
import com.merchant.ops.entity.Employee;
import com.merchant.ops.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);



    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
/*
启用禁用员工账号
@param status
@param id
 */
    void startOrStop(Integer status, Long id);

    /*
根据ID查询员工信息
@param id
@return
 */
    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);
}
