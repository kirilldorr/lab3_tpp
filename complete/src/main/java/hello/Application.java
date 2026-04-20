package hello;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@GetMapping("/")
	public String showForm() {
		return "calculator";
	}

	@PostMapping("/")
	public String calculate(@RequestParam("expression") String expression, Model model) {
		try {
			Expression e = new ExpressionBuilder(expression).build();
			double result = e.evaluate();
			model.addAttribute("result", expression + " = " + result);
		} catch (Exception ex) {
			model.addAttribute("error", "uncorrect expression");
		}
		return "calculator";
	}
}