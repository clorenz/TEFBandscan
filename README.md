Utility to manually or automatically create a bandscan for TEF6686 based radios.

Requires a [patched version](https://github.com/clorenz/TEF6686_ESP32/tree/force-beep) of 
the firmware with the following changes:

- Return the S/N ratio
- Allow to play a short beep when sending the command `L` via Wifi/XDRGtk protocol

Currently, it is only tested to run in IntelliJ IDEA.


## XDRGtk Protocol

### Read

- *S*: Status
  - First character: `s` or `m`: Stereo flag. `M` indicated, that stereo was toggled off
  - First comma separated value: Signal strength (dBµV + 11.25)
  - Second comma separated value: CCI (co-channel interference, based on multipath meter) in percent
  - Third comma separated value: ACI (adjacent channel = interference above the RDS subcarriers) in percent
  - Fourth comma separated value: Current bandwidth in kHZ

### Write (and read the same as confirmation)

- *A*: AGC
  - *A0*: Highest
  - *A1*: High
  - *A2*: Medium
  - *A3*: Low

- *B*: Stereo toggle
  - *B0*: 
  - *B1*:  

- *C*: Scan ?
  - C1: one direction
  - C2: other direction

- *D*: FM Deemphasis
  - *D0*: 50µs
  - *D1*: 75µs
  - *D2*: 0µs

- *F*: Bandwidth presets
  - *F0*: 0kHz (auto?)
  - *F1*: 56kHz 
  - *F2*: 64kHz
  - *F3*: 72kHz
  - *F4*: 84kHz
  - *F5*: 97kHz
  - *F6*: 114kHz
  - *F7*: 133kHz
  - *F8*: 151kHz
  - *F9*: 168kHz
  - *F10*: 184kHz
  - *F11*: 200kHz
  - *F12*: 217kHz
  - *F13*: 236kHz
  - *F14*: 254kHz
  - *F15*: 287kHz
  - *F16*: 311kHz

- *G*
  - *G00*: iMS + EQ off
  - *G01*: iMS on
  - *G10*: EQ on
  - *G11*: iMS + EQ on

- *M*: AM Frequency?

- *Q*: Squelch - Values: 0-100 and -1 for silent

- *S*: Scanning
  - *Sa*: Start frequency in kHz
  - *Sb*: End frequency in kHz
  - *Sc*: Steps in kHz
  - *Sf*: Filters:
    - *Sf-1": Set auto bandwidth  
    - *Sf0*: Set bandwidth to 56kHz (see F1)
    - *Sf1*: Set bandwidth to 72kHz (see F3)
    - *Sf3*: Set bandwidth to 114kHz (see F6)
    - *Sf4*: Set bandwidth to 133kHz (see F7)
    - *Sf5*: Set bandwidth to 151kHz (see F8)
    - *Sf7*: Set bandwidth to 168kHz (see F9)
    - *Sf8*: Set bandwidth to 184kHz (see F10)
    - *Sf9*: Set bandwidth to 200kHz (see F11)
    - *Sf10*: Set bandwidth to 217kHz (see F12)
    - *Sf11*: Set bandwidth to 236kHz (see F13)
    - *Sf12*: Set bandwidth to 254kHz (see F14)
    - *Sf13*: Set bandwidth to 287kHz (see F15)
    - *Sf15*: Set bandwidth to 311kHz (see F16)
    - *Sf26*: Set bandwidth to 64kHz (see F2)
    - *Sf28*: Set bandwidth to 84kHz (see F4)
    - *Sf29*: Set bandwidth to 97kHz (see F5)

- *T*: Tuning frequency in kHz, e.g. 87500

- *W*: Bandwidth in kHz (only values from preset (F0...F16) are possible)

- *Y*: Set volume

- *x*: Print out current data: Frequency (T) and  stereo toggle (B)

- *X*: Kind of reset?

- *Z* (Z0..Z3): Switch antennas

Custom addition:

- *L* Play a short beep (e.g. to indicate, that a log entry was made)