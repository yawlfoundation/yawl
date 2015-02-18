package org.yawlfoundation.yawl.elements.predicate;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public class CostPredicateEvaluator implements PredicateEvaluator {

    private CostGatewayClient _client;
    private String _handle;

    private static final Pattern PATTERN = Pattern.compile(
            "cost\\(\\s*(case\\(\\s*((|\\d+|\\d+-\\d+|max|min|average|" +
            "(dow|last|first|random)\\s+\\d+|(from|to)\\s+\\d{4}-[01]\\d-[0-3]\\d)" +
            "(|\\s*,\\s*))*\\s*\\))?(|\\s*,\\s*)" +
            "(task\\((['\"]*\\w+\\s*['\"]*(|,|\\s)\\s*)+\\))?(|\\s*,\\s*)" +
            "(resource\\((['\"]*\\w+\\s*['\"]*(|,|\\s)\\s*)+\\))?\\s*\\)");


    public CostPredicateEvaluator() {
        CostGatewayClient _client = new CostGatewayClient();
    }


    public boolean accept(String predicate) {
        return predicate != null && PATTERN.matcher(predicate).find();
    }


    public String substituteDefaults(String predicate) {
        for (String expression : extract(predicate)) {
            predicate = predicate.replace(expression, "0");
        }
        return predicate;
    }


    public String replace(YDecomposition decomposition, String predicate,
                                YIdentifier token) {
        for (String expression : extract(predicate)) {
            String result = calculate(decomposition, expression, token);
            predicate = predicate.replace(expression, result);
        }
        return predicate;
    }


    private void connect() throws IOException {
        if (!checkHandle()) {
            _handle = _client.connect("admin", "YAWL");
        }
    }


    private boolean checkHandle() throws IOException {
        return _handle != null && successful(_client.checkConnection(_handle));
    }


    private boolean successful(String msg) {
        return !(msg == null || msg.length() == 0 || msg.contains("<failure>"));
    }


    private List<String> extract(String predicate) {
        List<String> matches = new ArrayList<String>();
        Matcher matcher = PATTERN.matcher(predicate);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }


    public String calculate(YDecomposition decomposition, String expression,
                            YIdentifier token) {
        try {
            connect();
            return String.valueOf(_client.calculate(
                    decomposition.getSpecification().getSpecificationID(),
                    token.getId(), expression, _handle));
        } catch (IOException ioe) {
            Logger.getLogger(this.getClass()).error(ioe.getMessage());
            return "0";
        }
    }

}
