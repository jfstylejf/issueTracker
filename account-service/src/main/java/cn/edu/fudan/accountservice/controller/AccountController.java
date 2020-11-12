package cn.edu.fudan.accountservice.controller;

import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.ResponseBean;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.service.AccountService;
import cn.edu.fudan.accountservice.util.CookieUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

//@ApiOperation注解 value添加该api的说明，用note表示该api的data返回类型，httpMethod表示请求方式
//@ApiImplicitParams 参数列表集合注解
//@ApiImplicitParam 参数说明接口 对应api中@RequestParam作解释说明 可显示在swagger2-ui页面上
//  name表示参数名 value表示对参数的中文解释 dataType表示该参数类型 required表示该参数是否必须 defaultValue提供测试样例
//  具体@ApiImplicitParam其他属性ctrl+click 点进源码


@RestController
@CrossOrigin
@RequestMapping("/user")
public class AccountController {

    private AccountService accountService;

    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @ApiOperation(value="检查用户名是否存在",notes="@return boolean",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountName", value = "开发人员姓名", dataType = "String", required = true,defaultValue = "chenyuan"),
    })
    @GetMapping("/account-name/check")
    public Object checkUserName(@RequestParam("accountName") String accountName) {
        return new ResponseBean(200, "success", accountService.isAccountNameExist(accountName));
    }

    @ApiOperation(value="检查邮箱是否存在",notes="@return boolean",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "开发人员邮箱", dataType = "String", required = true,defaultValue = "chenyuan@163.com"),
    })
    @GetMapping("/email/check")
    public Object checkEmail(@RequestParam("email") String email) {
        return new ResponseBean(200, "success", accountService.isEmailExist(email));
    }

    /* 用户昵称=真实姓名=界面显示姓名 */
    @ApiOperation(value="检查用户昵称是否存在",notes="@return boolean",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nickName", value = "开发人员昵称", dataType = "String", required = true,defaultValue = "王贵成"),
    })
    @GetMapping("/nick-name/check")
    public Object checkNickName(@RequestParam("nickName") String nickName) {
        return new ResponseBean(200, "success", accountService.isNameExist(nickName));
    }

    @ApiOperation(value="获取用户在职状态",notes="@return Object",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "开发人员姓名列表", dataType = "List<String>", required = true,defaultValue = "[\"admin\",\"chenyuan\",\"王贵成\"]"),
    })
    @GetMapping("/status")
    public Object getStatusByName(@RequestBody List<String> name) {
        return new ResponseBean(200, " ",accountService.getStatusByName(name));
    }

    @ApiOperation(value="获取用户信息",notes="@return List<Account>",httpMethod = "GET")
    @GetMapping("/status/getData")
    public Object getAccountStatus(){
        return new ResponseBean(200, " ",accountService.getAccountStatus());
    }

    @ApiOperation(value="更改用户角色、部门、在职状态",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statusInfo", value = "用户角色、部门、在职状态", dataType = "List<Account>", required = true,defaultValue = "[\"P\",\"工程开发部\",\"1\"]"),
    })
    @PutMapping(value = "/status/dep/role")
    public Object updateAccountStatus(@RequestBody List<Account> statusInfo){
        try{
            accountService.updateAccountStatus(statusInfo);
            return new ResponseBean(200, "Successful!", null);
        }catch (Exception e){
            return new ResponseBean(401, "update failed! " + e.getMessage(), null);
        }

    }

    @ApiOperation(value="用户注册",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "开发人员信息列表", dataType = "Account", required = true)
    })
    @PostMapping("/register")
    public Object createUser(@RequestBody Account account) {
        try {
            accountService.addAccount(account);
            return new ResponseBean(200, "Congratulations！successful registration.", null);
        } catch (Exception e) {
            return new ResponseBean(401, "sign up failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="用户登录",notes="@return AccountInfo",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户姓名", dataType = "String", required = true,defaultValue = "admin"),
            @ApiImplicitParam(name = "password", value = "密码", dataType = "String", required = true,defaultValue = "YWRtaW4="),
    })
    @GetMapping(value = {"/login"})
    public Object login(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletResponse response) {
        ResponseBean responseBean = accountService.login(username, password);
        if (responseBean.getData() != null) {
            CookieUtil.addCookie(response, "userToken", responseBean.getData().toString(), 24 * 60 * 60);
        }
        return responseBean;
    }

    @ApiOperation(value="获取当前登录用户的uuid",notes="@return Account Uuid",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userToken", value = "用户token", dataType = "String", required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35"),
    })
    @GetMapping(value = "/accountId")
    public Object getAccountID(@RequestParam("userToken") String userToken) {
        return new ResponseBean(200, "success", accountService.getAccountByToken(userToken).getUuid());
    }

    /**
    *待测
     */
    @ApiOperation(value="获取当前登录用户登录状态",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userToken", value = "用户token", dataType = "String", required = true,defaultValue = "378f71b14cc4a0f1e6ce18bfe3a4028e"),
    })
    @GetMapping(value = "/auth/{userToken}")
    public Object auth(@PathVariable("userToken") String userToken) {
        if (accountService.authByToken(userToken)) {
            return new ResponseBean(200, "auth pass", null);
        } else {
            return new ResponseBean(401, "token time out,please login", null);
        }
    }

    @ApiOperation(value="获取当前登录用户的权限和uuid",notes="@return Map<String,Object>",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userToken", value = "用户token", dataType = "String", required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35"),
    })
    @GetMapping(value = "/right/{userToken}")
    @CrossOrigin
    public Object right(@PathVariable("userToken") String userToken) {
        if (accountService.authByToken(userToken)) {
            return new ResponseBean(200, "success", accountService.getRightByToken(userToken));
        } else {
            return new ResponseBean(401, "token time out,please login", null);
        }
    }

    @ApiOperation(value="获取用户ID",notes="@return List<String>",httpMethod = "GET")
    @GetMapping(value = "/accountIds")
    public Object getAccountIds() {
        return new ResponseBean(200, "success", accountService.getAllAccountId());
    }

    @ApiOperation(value="通过姓名获取用户组别",notes="@return List<String>",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountName", value = "开发人员姓名", dataType = "String", required = true,defaultValue = "chenyuan"),
    })
    @GetMapping(value = "/accountGroups")
    public Object getGroupsByAccountName(@RequestParam("accountName") String accountName){
        return new ResponseBean(200, "success",accountService.getGroupsByAccountName(accountName));
    }

    /**
    *待测
     */
    @ApiOperation(value="更新工具",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tools", value = "工具", dataType = "List<Tool>", required = true,defaultValue = "[\"sonarqube\",\"findbugs\"]"),
    })
    @PostMapping(value = "/updateTools")
    public ResponseBean updateToolsEnable(@RequestBody List<Tool> tools){
        try{
            accountService.updateToolsEnable(tools);
            return new ResponseBean<>(200, "Successful!", null);
        }catch (Exception e){
            return new ResponseBean<>(401, "update failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取工具列表",notes="@return List<Tool>",httpMethod = "GET")
    @GetMapping(value = "/tools")
    public Object getTools(){
        return new ResponseBean(200, "success",accountService.getTools());
    }

    @ApiOperation(value="获取用户姓名",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountId", value = "用户ID", dataType = "String", required = true,defaultValue = "1"),
    })
    @GetMapping(value = "/accountName")
    public Object getAccountNameById(@RequestParam("accountId") String accountId){
        return new ResponseBean(200, "success",accountService.getAccountNameById(accountId));
    }

    @ApiOperation(value="自动更新人员列表",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gitname", value = "更新后的gitname列表", dataType = "List<String>", required = true)
    })
    @PostMapping(value = "/account")
    public ResponseBean autoUpdateAccount(@RequestBody List<String> gitname)
    {
        try{
            accountService.addNewAccounts(gitname);
            return new ResponseBean<>(200, "receive!", null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401, "failed! " + e.getMessage(), null);
        }
    }

}
