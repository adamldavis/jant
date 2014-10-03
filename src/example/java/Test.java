import com.google.common.base.Optional;


public class Test {

	public static void main(String[] args) {
		Optional<String> opt = (args.length > 1) ? Optional.of(args[0]) : Optional.<String>absent();
		
		System.out.println("It works!");
		if (opt.isPresent()) {
			System.out.println("Input: " + opt.get());
		}
	}
}
