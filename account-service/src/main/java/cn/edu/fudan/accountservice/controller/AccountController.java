package cn.edu.fudan.accountservice.controller;

import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.ResponseBean;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.service.AccountService;
import cn.edu.fudan.accountservice.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@EnableAutoConfiguration
@RequestMapping("/user")
public class AccountController {

    private AccountService accountService;

    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account-name/check")
    @CrossOrigin
    public Object checkUserName(@RequestParam("accountName") String accountName) {
        return new ResponseBean(200, "success", accountService.isAccountNameExist(accountName));
    }

    @GetMapping("/email/check")
    @CrossOrigin
    public Object checkEmail(@RequestParam("email") String email) {
        return new ResponseBean(200, "success", accountService.isEmailExist(email));
    }

    @GetMapping("/nick-name/check")
    @CrossOrigin
    public Object checkNickName(@RequestParam("nickName") String nickName) {
        return new ResponseBean(200, "success", accountService.isNameExist(nickName));
    }

    @GetMapping("/status")
    @CrossOrigin
    public Object getStatusByName(@RequestBody List<String> name) {
        return new ResponseBean(200, " ",accountService.getStatusByName(name));
    }


    @PostMapping("/register")
    @CrossOrigin
    public Object createUser(@RequestBody Account account) {
        try {
            accountService.addAccount(account);
            return new ResponseBean(200, "Congratulations！successful registration.", null);
        } catch (Exception e) {
            return new ResponseBean(401, "sign up failed! " + e.getMessage(), null);
        }
    }

    @GetMapping(value = {"/login"})
    @CrossOrigin
    public Object login(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletResponse response) {
        ResponseBean responseBean = accountService.login(username, password);
        if (responseBean.getData() != null) {
            CookieUtil.addCookie(response, "userToken", responseBean.getData().toString(), 24 * 60 * 60);
        }
        return responseBean;
    }

    @GetMapping(value = "/accountId")
    @CrossOrigin
    public Object getAccountID(@RequestParam("userToken") String userToken) {
        return accountService.getAccountByToken(userToken).getUuid();
    }

    @GetMapping(value = "/auth/{userToken}")
    @CrossOrigin
    public Object auth(@PathVariable("userToken") String userToken) {
        if (accountService.authByToken(userToken)) {
            return new ResponseBean(200, "auth pass", null);
        } else {
            return new ResponseBean(401, "token time out,please login", null);
        }
    }

    @GetMapping(value = "/accountIds")
    public Object getAccountIds() {
        return accountService.getAllAccountId();
    }

    @GetMapping(value = "/accountGroups")
    @CrossOrigin
    public Object getGroupsByAccountName(@RequestParam("accountName") String accountName){
        return accountService.getGroupsByAccountName(accountName);
    }

    @PostMapping(value = "/updateTools")
    @CrossOrigin
    public Object updateToolsEnable(@RequestBody List<Tool> tools){
        try{
            accountService.updateToolsEnable(tools);
            return new ResponseBean(200, "Successful!", null);
        }catch (Exception e){
            return new ResponseBean(401, "update failed! " + e.getMessage(), null);
        }

    }

    @GetMapping(value = "/tools")
    @CrossOrigin
    public Object getTools(){
        return accountService.getTools();
    }

    @GetMapping(value = "/accountName")
    @CrossOrigin
    public Object getAccountNameById(@RequestParam("accountId") String accountId){
        return accountService.getAccountNameById(accountId);
    }
}