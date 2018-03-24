package parser.ast.match;

public class Labels {
    public static final String MetricName   = "__name__";
//    public static final String AlertName    = "alertname";
//    public static final String BucketLabel  = "le";
//    public static final String InstanceName = "instance";

    // AlertNameLabel is the name of the label containing the an alert's name.
//    AlertNameLabel = "alertname"

    // ExportedLabelPrefix is the prefix to prepend to the label names present in
    // exported metrics if a label of the same name is added by the server.
//    ExportedLabelPrefix = "exported_"

    // MetricNameLabel is the label name indicating the metric name of a
    // timeseries.
    public static final String MetricNameLabel = "__name__";

    // SchemeLabel is the name of the label that holds the scheme on which to
    // scrape a target.
//    SchemeLabel = "__scheme__"

    // AddressLabel is the name of the label that holds the address of
    // a scrape target.
//    AddressLabel = "__address__"

    // MetricsPathLabel is the name of the label that holds the path on which to
    // scrape a target.
//    MetricsPathLabel = "__metrics_path__"

    // ReservedLabelPrefix is a prefix which is not legal in user-supplied
    // label names.
//    ReservedLabelPrefix = "__"

    // MetaLabelPrefix is a prefix for labels that provide meta information.
    // Labels with this prefix are used for intermediate label processing and
    // will not be attached to time series.
//    MetaLabelPrefix = "__meta_"

    // TmpLabelPrefix is a prefix for temporary labels as part of relabelling.
    // Labels with this prefix are used for intermediate label processing and
    // will not be attached to time series. This is reserved for use in
    // Prometheus configuration files by users.
//    TmpLabelPrefix = "__tmp_"

    // ParamLabelPrefix is a prefix for labels that provide URL parameters
    // used to scrape a target.
//    ParamLabelPrefix = "__param_"

    // JobLabel is the label name indicating the job from which a timeseries
    // was scraped.
//    JobLabel = "job"

    // InstanceLabel is the label name used for the instance label.
//    InstanceLabel = "instance"

    // BucketLabel is used for the label that defines the upper bound of a
    // bucket of a histogram ("le" -> "less or equal").
    // QuantileLabel is used for the label that defines the quantile in a
    // summary.
//    public static final String QuantileLabel = "quantile";


}
