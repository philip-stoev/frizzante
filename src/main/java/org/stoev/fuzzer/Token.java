public class Token {
	private TokenType tokenType;

	public Token(final TokenType tt) {
		this.tokenType = tt;
	}

	public final TokenType getTokenType() {
		return this.tokenType;
	}
}

enum TokenType {
	TOKEN_LITERAL
}


