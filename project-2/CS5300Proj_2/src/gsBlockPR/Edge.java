package gsBlockPR;

public class Edge {
	public Integer srcID;
	public Integer destID;
	public Float srcPR;
	public Integer srcDegree;
	
	public Edge(Integer srcID, Integer destID, Float srcPR, Integer srcNumOutLinks)
	{
		this.srcID = srcID;
		this.destID = destID;
		this.srcPR = srcPR;
		this.srcDegree = srcNumOutLinks;
	}
	
	public Edge(String line)
	{
		line = line.trim();
		String[] parts = line.split(" ");
		this.srcID = Integer.parseInt(parts[0]);
		this.destID = Integer.parseInt(parts[1]);
		this.srcPR = Float.parseFloat(parts[2]);
		this.srcDegree = Integer.parseInt(parts[3]);
	}
	
	@Override
	public String toString()
	{
		return srcID + " " + destID + " " + srcPR + " " + srcDegree;
	}
}
