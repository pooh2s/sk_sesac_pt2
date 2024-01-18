package org.example.hacking02_sk.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.example.hacking02_sk.model.Location;
import org.example.hacking02_sk.model.User;
import org.example.hacking02_sk.model.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("member")
public class UserController {
	
	@Autowired
    private UserDAO userDAO;
	
	// 회원가입 약관
	@GetMapping("joinInfo")
	public String joinInfo() {
		return "member/joinInfo";
	}
	
	// 회원가입
	@RequestMapping("join")
	public String join(Model model) {
		model.addAttribute("user", new User()); //빈 객체보내기
		return "member/join";
	}
	
	@PostMapping("join")
    public ModelAndView join(User user, HttpServletResponse response) throws IOException {
		ModelAndView mav = new ModelAndView();
		if (user.getMyname().equals("") || user.getMyid().equals("") || user.getMypw().equals("") || 
				user.getMyemail().equals("") || user.getMylocation().equals("") || user.getMyphone().equals("") ||
				user.getMyaccpw().equals("") || user.getMysid().equals("")) {
			mav.setViewName("member/joinFail");
			mav.addObject("message", "모든 항목을 입력해야 회원가입이 가능합니다.");
			return mav;
		}
		else {
			int result = userDAO.signup(user);
			
			if (result > 0) { // 회원가입 성공
	        	mav.setViewName("redirect:/");
				return mav;
			}
		}
		return null;
    }
	
    // 아이디 중복 체크
    @ResponseBody
    @PostMapping("usercheck")
    public Map<String, String> userCheck(@RequestBody HashMap<String, String> myid) {
        String result = "N";

        System.out.println("넘어온 값 : " + myid);
        System.out.println("Value 값 : " + myid.get("myid"));
        System.out.println(myid.get("myid").getClass());
        Map<String, String> check = new HashMap<>();
        int flag = userDAO.userCheck(myid.get("myid"));
      
        if(flag == 0) {
        	result ="Y";
        }
        else if(flag == -1) {
        	result = "E";
        }
        check.put("result", result);
        System.out.println("result값 : " + result);
        return check;
    }
	
    // 로그인
	@RequestMapping("login")
	public String login() {
		return "member/login";
	}
	
	HashMap<String, String> sessions = new HashMap<String, String>();

    @PostMapping("login")
    public ModelAndView loginAction(User user, HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();
        int result = userDAO.login(user.getMyid(), user.getMypw());
		request.getSession().invalidate();

        if (result == 1) { //로그인 성공
			HttpSession session = request.getSession();
			user = userDAO.getUser(user.getMyid(), user.getMypw());
			session.setAttribute("user", user);
			session.setMaxInactiveInterval(60);
			sessions.put(session.getId(), ((User)(session.getAttribute("user"))).getMyname());
			mav.setViewName("redirect:/");
			return mav;
        }
        else if (result == 0) {
			mav.setViewName("member/loginFail");
			mav.addObject("message", "패스워드 불일치");
			return mav;
        }
        else if (result == -1) {
			mav.setViewName("member/loginFail");
			mav.addObject("message", "존재하지 않는 ID");
			return mav;
        }
        else if (result == -2) {
			mav.setViewName("member/loginFail");
			mav.addObject("message", "DB 에러");
			return mav;
        }
        return null; // maybe not reachable
    }

	@RequestMapping("logout")
    public ModelAndView logoutAction(User user, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession(false);
		if(session == null) { // 세션이 만료된 경우 (maybe not reachable)
			mav.addObject("message", "세션이 만료되었습니다.");
		}
		else { // 로그아웃 시도
			if (session.getAttribute("user") != null)
				sessions.remove(session.getId());
			session.invalidate();
			mav.addObject("message", "로그아웃 하였습니다.");
		}
		mav.setViewName("/member/logout");
		return mav;
	}

	// test
	@RequestMapping("/sessions")
	@ResponseBody
	public String sessionList() {
		StringBuilder sb = new StringBuilder("{");
		for(String key : sessions.keySet()) {
			sb.append(key + " : " + sessions.get(key) + "\n");
		}
		sb.append("}");
		return sb.toString();
	}
    
    // 주소 검색
	@RequestMapping("join/popup")
	public String findLocation(@RequestParam(value = "keyword", required = false) String keyword, Model model) throws SQLException {
		if (keyword != null) {
			List<Location> locations = userDAO.findLocation(keyword);
			System.out.println(userDAO.findLocation(keyword));
			model.addAttribute("locations", locations);
		}
		return "popup/findlocation";
	}
}