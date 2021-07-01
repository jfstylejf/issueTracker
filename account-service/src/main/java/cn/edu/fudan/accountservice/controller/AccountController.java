package cn.edu.fudan.accountservice.controller;

import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.AccountVO;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.service.AccountService;
import cn.edu.fudan.accountservice.util.CookieUtil;
import cn.edu.fudan.accountservice.util.PagedGridResult;
import cn.edu.fudan.common.http.ResponseEntity;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//@ApiOperation注解 value添加该api的说明，用note表示该api的data返回类型，httpMethod表示请求方式
//@ApiImplicitParams 参数列表集合注解
//@ApiImplicitParam 参数说明接口 对应api中@RequestParam作解释说明 可显示在swagger2-ui页面上
//  name表示参数名 value表示对参数的中文解释 dataType表示该参数类型 required表示该参数是否必须 defaultValue提供测试样例
//  具体@ApiImplicitParam其他属性ctrl+click 点进源码


/**
 * @author fancying
 */
@RestController
@CrossOrigin
@RequestMapping("/user")
public class AccountController {
    private final String TOKEN = "token";

    @Autowired
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
    public ResponseEntity<Boolean> checkUserName(@RequestParam("accountName") String accountName) {
        try {
            Boolean result = accountService.isAccountNameExist(accountName);
            if(!result){
                return new ResponseEntity<>(412, "account name is duplicate!", false);
            }else{
                return new ResponseEntity<>(200, "account name can be used!", true);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(401, "check failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="检查邮箱是否存在",notes="@return boolean",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "开发人员邮箱", dataType = "String", required = true,defaultValue = "chenyuan@163.com"),
    })
    @GetMapping("/email/check")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
        try {
            Boolean result = accountService.isEmailExist(email);
            if(!result){
                return new ResponseEntity<>(412, "email is duplicate!", false);
            }else{
                return new ResponseEntity<>(200, "email can be used!", true);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(401, "check failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取用户在职状态",notes="@return Object",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountName", value = "开发人员姓名列表", dataType = "List<String>", required = true,defaultValue = "[\"admin\",\"chenyuan\",\"heyue\"]"),
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Integer>> getStatusByName(@RequestBody List<String> accountNameList) {
        try {
            Map<String, Integer> result = accountService.getStatusByName(accountNameList);
            return new ResponseEntity<>(200, "get account status success", result);
        } catch (Exception e) {
            return new ResponseEntity<>(401, "get failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取用户信息",notes="@return List<Account>",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account_status", value = "人员在职状态", dataType = "String"),
            @ApiImplicitParam(name = "account_names", value = "名字搜索", dataType = "String"),
            @ApiImplicitParam(name = "is_whole", value = "是否获取所有数据（不进行分页）", dataType = "Boolean", required = false,defaultValue = "0"),
            @ApiImplicitParam(name = "page", value = "分页的第几页", dataType = "Integer", required = false,defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "分页中每页的大小", dataType = "Integer", required = false,defaultValue = "10"),
            @ApiImplicitParam(name = "order", value = "要排序的字段", dataType = "String", required = false,defaultValue = "developerName"),
            @ApiImplicitParam(name = "asc", value = "是否升序", dataType = "Boolean", required = false,defaultValue = "1")
    })

    @GetMapping("/status/getData")
    public ResponseEntity<Object> getAccountStatus(
                                   @RequestParam(value = "account_status", defaultValue = "1") String accountStatus,
                                   @RequestParam(value = "account_names", required = false) String accountNames,
                                   @RequestParam(value = "is_whole", required = false, defaultValue = "0") Boolean isWhole,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                   @RequestParam(value = "ps", required = false, defaultValue = "30") Integer pageSize,
                                   @RequestParam(value = "order", required = false) String order,
                                   @RequestParam(value = "asc", required = false, defaultValue = "1") Boolean isAsc){
        try{
            // 获取所有数据，不进行分页
            if (isWhole) {
                return new ResponseEntity<>(200, "success!", accountService.getAccountList(accountStatus, accountNames));
            }
            // 否则，获取分页数据
            PagedGridResult result = accountService.getAccountList(accountStatus, accountNames, page, pageSize, order, isAsc);
            return new ResponseEntity<>(200, "get account success!", result);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(401, "get account failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="更改用户角色、部门、在职状态",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statusInfo", value = "用户角色、部门、在职状态", dataType = "List<Account>", required = true,defaultValue = "[\"P\",\"工程开发部\",\"1\"]"),
    })
    @PutMapping(value = "/status/dep/role")
    public ResponseEntity<Object> updateAccountStatus(@RequestBody List<Account> statusInfo){
        try{
            accountService.updateAccountStatus(statusInfo);
            return new ResponseEntity<>(200, "update success!", null);
        }catch (Exception e){
            return new ResponseEntity<>(401, "update failed! " + e.getMessage(), null);
        }

    }

    @ApiOperation(value="用户注册",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "开发人员信息列表", dataType = "Account", required = true)
    })
    @PostMapping("/register")
    public ResponseEntity<Object> createUser(@RequestBody Account account) {
        try {
            accountService.addAccount(account);
            return new ResponseEntity<>(200, "Congratulations！successful registration.", null);
        } catch (Exception e) {
            return new ResponseEntity<>(401, "sign up failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="用户登录",notes="@return AccountVO",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户姓名", dataType = "String", required = false,defaultValue = "admin"),
            @ApiImplicitParam(name = "password", value = "密码", dataType = "String", required = true,defaultValue = "YWRtaW4="),
            @ApiImplicitParam(name = "email", value = "用户邮箱", dataType = "String", required = false,defaultValue = "123@fudan.edu.cn")
    })
    @GetMapping(value = {"/login"})
    public ResponseEntity<AccountVO> login(@RequestParam(value = "username", required = false) String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "email", required = false) String email ,
                        HttpServletResponse response) {
        try {
            AccountVO accountVO = accountService.login(username, password, email);
            ResponseEntity<AccountVO> responseBean = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(), null);
            if (accountVO != null) {
                CookieUtil.addCookie(response, "userToken", accountVO.getToken(), 24 * 60 * 60);
                responseBean.setCode(HttpStatus.OK.value());
                responseBean.setMsg(HttpStatus.OK.name());
                responseBean.setData(accountVO);
            } else {
                return new ResponseEntity<>(412, "username or password is wrong!", null);
            }
            return responseBean;
        }catch (Exception e){
            return new ResponseEntity<>(401, "login failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="密码重置", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户姓名", dataType = "String", required = true),
            @ApiImplicitParam(name = "password", value = "新密码", dataType = "String", required = true)
    })
    @PutMapping(value = {"/password"})
    public ResponseEntity<Boolean> passwordReset(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                HttpServletResponse response) {
        try{
            boolean result = accountService.passwordReset(username, password);
            if(!result){
                return new ResponseEntity<>(412, "account not exist!", null);
            }
            return new ResponseEntity<>(200, "reset success!", null);
        }catch (Exception e){
            return new ResponseEntity<>(401, "reset failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取当前登录用户的uuid",notes="@return Account Uuid",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userToken", value = "用户token", dataType = "String", required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35"),
    })
    @GetMapping(value = "/accountId")
    public ResponseEntity<String> getAccountID(@RequestParam("userToken") String userToken) {
        try{
            Account account = accountService.getAccountByToken(userToken);
            if(account == null){
                return new ResponseEntity<>(412, "account not exist!", null);
            }
            return new ResponseEntity<>(200, "success", account.getUuid());
        }catch (Exception e){
            return new ResponseEntity<>(401, "get info failed! " + e.getMessage(), null);
        }
    }

    /**
    *待测
     */
    @ApiOperation(value="获取当前登录用户登录状态",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userToken", value = "用户token", dataType = "String", required = true,defaultValue = "378f71b14cc4a0f1e6ce18bfe3a4028e"),
    })
    @GetMapping(value = "/auth/{userToken}")
    public ResponseEntity<Map<String, Object>> auth(@PathVariable("userToken") String userToken) {
        try {
            Map<String, Object> result = accountService.authByToken(userToken);
            if (result != null) {
                return new ResponseEntity<>(200, "auth pass success", result);
            } else {
                return new ResponseEntity<>(401, "token time out,please login", null);
            }
        }catch (Exception e){
            return new ResponseEntity<>(401, "get info failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取当前登录用户的权限和uuid",notes="@return Map<String,Object>",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userToken", value = "用户token", dataType = "String", required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35"),
    })
    @GetMapping(value = "/right/{userToken}")
    @CrossOrigin
    public ResponseEntity<Object> right(@PathVariable("userToken") String userToken) {
        try {
            Object result = accountService.authByToken(userToken);
            if (result != null) {
                return new ResponseEntity<>(200, "success", true);
            } else {
                return new ResponseEntity<>(401, "token time out,please login", null);
            }
        }catch (Exception e){
            return new ResponseEntity<>(401, "get info failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取用户ID",notes="@return List<String>",httpMethod = "GET")
    @GetMapping(value = "/accountIds")
    public Object getAccountIds() {
        try {
            return new ResponseEntity<>(200, "success", accountService.getAllAccountId());
        }catch (Exception e){
            return new ResponseEntity<>(401, "get info failed! " + e.getMessage(), null);
        }
    }

    /**
    *待测
     */
    @ApiOperation(value="更新工具",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tools", value = "工具", dataType = "List<Tool>", required = true,defaultValue = "[\"sonarqube\",\"findbugs\"]"),
    })
    @PostMapping(value = "/updateTools")
    public ResponseEntity<Object> updateToolsEnable(@RequestBody List<Tool> tools){
        try{
            accountService.updateToolsEnable(tools);
            return new ResponseEntity<>(200, "success!", null);
        }catch (Exception e){
            return new ResponseEntity<>(401, "update failed! " + e.getMessage(), null);
        }
    }

    /**
     * fixme 放到scan service 中
     */
    @Deprecated
    @ApiOperation(value="获取工具列表",notes="@return List<Tool>",httpMethod = "GET")
    @GetMapping(value = "/tools")
    public ResponseEntity<List<Tool>> getTools(){
        try {
            return new ResponseEntity<>(200, "success", accountService.getTools());
        }catch (Exception e){
            return new ResponseEntity<>(401, "get info failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取用户姓名",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountId", value = "用户ID", dataType = "String", required = true,defaultValue = "1"),
    })
    @GetMapping(value = "/accountName")
    public ResponseEntity<String> getAccountNameById(@RequestParam("accountId") String accountId){
        try {
            String result = accountService.getAccountNameById(accountId);
            if (result == null) {
                return new ResponseEntity<>(412, "account not exist!", null);
            }
            return new ResponseEntity<>(200, "success", result);
        }catch (Exception e){
            return new ResponseEntity<>(401, "get info failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取用户姓名",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account_name", value = "用户名", dataType = "String", required = true),
            @ApiImplicitParam(name = "need_admin", value = "是否需要显示admin", dataType = "Boolean", required = true)
    })
    @GetMapping(value = "/account/name")
    public ResponseEntity<Account> getAccount(@RequestParam("account_name") String accountName,
                                             @RequestParam(value = "need_admin", defaultValue = "false") Boolean needAdmin){
        try {
            Account result = accountService.getAccountByName(accountName, needAdmin);
            if(result == null){
                return new ResponseEntity<>(412, "account not exist!", null);
            }
            return new ResponseEntity<>(200, "get account success!", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(401, "get account failed!", null);
        }
    }

    @ApiOperation(value="自动更新人员列表",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gitNames", value = "更新后的gitname列表", dataType = "List<String>", required = true)
    })
    @PostMapping(value = "/account")
    public ResponseEntity<Boolean> autoUpdateAccount(@RequestBody List<String> gitNames) {
        try{
            Boolean result = accountService.addNewAccounts(gitNames);
            if(!result){
                return new ResponseEntity<>(412, "can't insert reduplicated account!", null);
            }
            return new ResponseEntity<>(200, "receive success!", null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(401, "failed! " + e.getMessage(), null);
        }
    }


    @ApiOperation(value="获取给定条件下的开发人员（聚合后）列表",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuids", value = "repo库", dataType = "String", required = false,defaultValue = "a140dc46-50db-11eb-b7c3-394c0d058805"),
            @ApiImplicitParam(name = "account_status", value = "人员在职状态", dataType = "String"),
            @ApiImplicitParam(name = "since", value = "起始时间", dataType = "String", required = false,defaultValue = "2020-01-01"),
            @ApiImplicitParam(name = "until", value = "结束时间", dataType = "String", required = false,defaultValue = "2020-12-31"),
            @ApiImplicitParam(name = "developers", value = "名字搜索", dataType = "String"),
            @ApiImplicitParam(name = "is_whole", value = "是否获取所有数据（不进行分页）", dataType = "Boolean", required = false,defaultValue = "0"),
            @ApiImplicitParam(name = "page", value = "分页的第几页", dataType = "Integer", required = false,defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "分页中每页的大小", dataType = "Integer", required = false,defaultValue = "10"),
            @ApiImplicitParam(name = "order", value = "要排序的字段", dataType = "String", required = false,defaultValue = "developerName"),
            @ApiImplicitParam(name = "asc", value = "是否升序", dataType = "Boolean", required = false,defaultValue = "1")
    })
    @GetMapping(value = "/developers")
    public Object getDeveloperList(@RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                   @RequestParam(value = "account_status", required = false, defaultValue = "1") String accountStatus,
                                   @RequestParam(value = "since", required = false) String since,
                                   @RequestParam(value = "until", required = false) String until,
                                   @RequestParam(value = "developers", required = false) String developers,
                                   @RequestParam(value = "is_whole", required = false, defaultValue = "0") Boolean isWhole,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                   @RequestParam(value = "ps", required = false, defaultValue = "30") Integer pageSize,
                                   @RequestParam(value = "order", required = false) String order,
                                   @RequestParam(value = "asc", required = false, defaultValue = "1") Boolean isAsc
                                   ){
        String[] repoListArr = repoUuids.split(",");
        List<String> repoList = Arrays.asList(repoListArr);
        if (StringUtils.isEmpty(repoUuids)) {
            repoList = null;
        }
        try{
            // 获取所有数据，不进行分页
            if (isWhole) {
                return new ResponseEntity<>(200, "success!", accountService.getDevelopers(repoList, since, until, accountStatus));
            }
            // 否则，获取分页数据
            PagedGridResult result = accountService.getDevelopers(repoList, since, until, developers, page, pageSize, order, isAsc, accountStatus);
            return new ResponseEntity<>(200, "success!", result);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(401, "failed! " + e.getMessage(), null);
        }
    }

    @ApiOperation(value="前端人员聚合", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "major_name", value = "主合并人姓名", dataType = "String", required = true),
            @ApiImplicitParam(name = "sub_name", value = "被合并人姓名", dataType = "String", required = true)
    })
    @PutMapping(value = {"/account/merge"})
    public ResponseEntity<List<String>> accountMerge(@RequestParam("major_name") String majorAccountName,
                               @RequestParam("sub_name") String subAccountName,
                               HttpServletRequest request) {
        try{
            List<String> accountGitname = accountService.accountMerge(majorAccountName, subAccountName, request.getHeader(TOKEN));
            return new ResponseEntity<>(200, "reset success!", accountGitname);
        }catch (Exception e){
            return new ResponseEntity<>(401, "reset failed! " + e.getMessage(), null);
        }
    }

}
