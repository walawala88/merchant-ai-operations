package com.merchant.ops.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.merchant.ops.constant.MessageConstant;
import com.merchant.ops.constant.PasswordConstant;
import com.merchant.ops.constant.StatusConstant;
import com.merchant.ops.context.BaseContext;
import com.merchant.ops.dto.EmployeeDTO;
import com.merchant.ops.dto.EmployeeLoginDTO;
import com.merchant.ops.dto.EmployeePageQueryDTO;
import com.merchant.ops.entity.Employee;
import com.merchant.ops.exception.AccountLockedException;
import com.merchant.ops.exception.AccountNotFoundException;
import com.merchant.ops.exception.PasswordErrorException;
import com.merchant.ops.mapper.EmployeeMapper;
import com.merchant.ops.result.PageResult;
import com.merchant.ops.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    /**
     * 新增员工
     * @param employeeDTO
     */

    public void save(EmployeeDTO employeeDTO) {
    Employee employee = new Employee();
    // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
    //设置创建时间
     //   employee.setCreateTime(LocalDateTime.now());
        //设置更新时间
      //  employee.setUpdateTime(LocalDateTime.now());



        //设置创建人ID
      //  employee.setCreateUser(BaseContext.getCurrentId());
        //设置更新人ID
      //  employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);
    }
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }
    /*
启用禁用员工账号
@param status
@param id
@return
 */
    @Override

    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /*
根据ID查询员工信息
@param id
@return
 */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }
/*
修改员工信息
@param employeeDTO
@return
 */
    @Override
    public void update(EmployeeDTO employeeDTO) {
    Employee employee = new Employee();
    BeanUtils.copyProperties(employeeDTO, employee);
   // employee.setUpdateTime(LocalDateTime.now());
  //  employee.setUpdateUser(BaseContext.getCurrentId());
    employeeMapper.update(employee);
    }
}
