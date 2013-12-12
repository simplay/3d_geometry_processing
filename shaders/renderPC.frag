#version 150
in vec4 frag_color;

out vec4 out_color;

void main()
{	
	// render the squared point as round point
	if (length(gl_PointCoord-vec2(0.5)) > 0.5)
    	discard;
	out_color = frag_color;
	// fade out towards border
	out_color.w = 2 - length(gl_PointCoord-vec2(0.5))*4;
}
